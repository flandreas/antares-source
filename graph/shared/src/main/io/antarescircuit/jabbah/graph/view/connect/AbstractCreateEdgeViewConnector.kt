package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.collection.Stack
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.NetView
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter.displayPortViewHighlight
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType.ORIGIN
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewFactory
import io.antarescircuit.jabbah.graph.view.port.PortView

/**
 * A base class of connectors that create a new [EdgeView].
 */
abstract class AbstractCreateEdgeViewConnector(
	private val edgeViewFactory: EdgeViewFactory,
	draggedEndpointType: EdgeViewEndpointType
) : AbstractConnector(draggedEndpointType) {

	companion object {
		private val LOG by logger(AbstractCreateEdgeViewConnector::class)
	}

	private var oldStatus: String? = null

	protected val isValidEdgeView: Boolean get() = edgeView != null && edgeView!!.isSufficientlyLarge

	/**
	 * The indices of the points in [edgeView] that have been manually set (i.e. adjusted) by the user.
	 * Organized as a [Stack] to support repetitive unrollment by pressing ESC.
	 */
	// Visible for testing
	var adjustment: EdgeViewAdjustmentView? = null
		private set

	protected abstract fun createAdjustment(): EdgeViewAdjustmentView

	protected fun replaceStatusBar(text: String) {
		oldStatus = Status.replace(StatusType.Tool, text)
	}

	protected fun resetStatusBar() {
		Status.set(StatusType.Tool, oldStatus)
	}

	protected fun beginAdjustment(context: EditInputEventContext) {
		adjustment = createAdjustment()
		context.drawingView.animationContainer.add(adjustment!!)
		context.drawingView.animationContainer.validate()
		context.editor.commandManager.active = false
	}

	protected fun endAdjustment(context: EditInputEventContext) {
		context.drawingView.animationContainer.remove(adjustment!!)
		adjustment!!.dispose()
		adjustment = null
		context.editor.commandManager.active = true
	}

	protected open fun getMoveAdjustedPointOrigDirs(layoutIndex: Int, allowContinuation: Boolean): Set<Direction>? {
		if (layoutIndex > 0) {
			val previousSegmentDir = edgeView!!.layout.type.getSegmentDirection(edgeView!!, layoutIndex - 1)
			return if (allowContinuation) {
				previousSegmentDir?.allButSet()
			} else {
				previousSegmentDir?.orthogonalSet()
			}
		}
		return null
	}

	protected fun getMoveAdjustedPointDestDirs(layoutIndex: Int, allowContinuation: Boolean): Set<Direction>? {
		if (layoutIndex < edgeView!!.segmentPointCount - 1) {
			val previousSegmentDir = edgeView!!.layout.type.getSegmentDirection(edgeView!!, layoutIndex)
			return if (allowContinuation) {
				previousSegmentDir?.allButSet()
			} else {
				previousSegmentDir?.orthogonalSet()
			}
		}
		return null
	}

	protected fun moveAdjustedPoint(context: EditInputEventContext) {
		when (draggedEndpointType) {
            ORIGIN -> moveAdjustedOriginPoint(context)
            EdgeViewEndpointType.DESTINATION -> moveAdjustedDestinationPoint(context)
        }
	}

	private fun moveAdjustedDestinationPoint(context: EditInputEventContext) {
		val p = context.location.add(context.editor.snapManager.snap(context.x, context.y))
		val layoutIndex = adjustment!!.model.current
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = layoutIndex,
			location = p,
			origDirs = getMoveAdjustedPointOrigDirs(layoutIndex, allowContinuation = false),
			destDirs = null)
	}

	private fun moveAdjustedOriginPoint(context: EditInputEventContext) {
		val p = context.location.add(context.editor.snapManager.snap(context.x, context.y))
		val layoutIndex = adjustment!!.model.current
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = layoutIndex,
			location = p,
			origDirs = null,
			destDirs = getMoveAdjustedPointDestDirs(layoutIndex, allowContinuation = false))
	}

	protected fun addAdjustedPoint(context: EditInputEventContext) {
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = context.location.add(context.editor.snapManager.snap(context.x, context.y)),
			null,
			null
		)

		adjustment!!.model.add()
		edgeView!!.validate()
	}

	protected fun adjustToTargetPortView(context: EditInputEventContext) {
		// Start highlighting current destination PortView
		val connPointAbs = targetPortView!!.owner!!.getPortConnectionPoint(targetPortView!!.port)
		displayPortViewHighlight(context.drawingView, connPointAbs, alternativeView = true)

		val ownLayoutIndex = adjustment!!.model.current
		when (draggedEndpointType) {
            ORIGIN -> adjustToTargetPortViewImpl(
				connPointAbs,
				setOf(draggedEndpointType.getDirectionForPortView(targetPortView!!)),
				getMoveAdjustedPointDestDirs(ownLayoutIndex, allowContinuation = true)
			)
            EdgeViewEndpointType.DESTINATION -> adjustToTargetPortViewImpl(
				connPointAbs,
				getMoveAdjustedPointOrigDirs(ownLayoutIndex, allowContinuation = true),
				setOf(draggedEndpointType.getDirectionForPortView(targetPortView!!))
			)
        }
	}

	private fun adjustToTargetPortViewImpl(snapLocation: Point2D, origDirs: Set<Direction>?, destDirs: Set<Direction>?) {
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = snapLocation,
			origDirs = origDirs,
			destDirs = destDirs
		)
		edgeView?.validate()
	}

	protected fun adjustToTargetEdgeView(context: EditInputEventContext) {
		targetEdgeView?.snap(context.x, context.y, draggedEndpointType == ORIGIN, context.editor.snapManager)?.let { snapResult ->
			targetEdgeViewSegmentIndex = snapResult.segmentIndex
			val ownLayoutIndex = adjustment!!.model.current
			val targetDirs = targetEdgeView!!.getSegmentDirection(targetEdgeViewSegmentIndex!!)?.orthogonalSet()
			when (draggedEndpointType) {
				ORIGIN -> adjustToTargetEdgeViewImpl(
					context,
					snapResult.location,
					targetDirs,
					getMoveAdjustedPointDestDirs(ownLayoutIndex, allowContinuation = true))
				EdgeViewEndpointType.DESTINATION -> adjustToTargetEdgeViewImpl(
					context,
					snapResult.location,
					getMoveAdjustedPointOrigDirs(ownLayoutIndex, allowContinuation = true),
					targetDirs)
			}
		}
	}

	private fun adjustToTargetEdgeViewImpl(context: EditInputEventContext, snapLocation: Point2D, origDirs: Set<Direction>?, destDirs: Set<Direction>?) {
		displayPortViewHighlight(context.drawingView, snapLocation, alternativeView = true)
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = snapLocation,
			origDirs = origDirs,
			destDirs = destDirs
		)
		edgeView?.validate()
	}

	// TODO: Almost equal to adjustToTargetPortView(), only different PortViewHighlight
	protected fun adjustToDenyingPortView(context: EditInputEventContext) {
		val connPointAbs = targetPortView!!.owner!!.getPortConnectionPoint(targetPortView!!.port)
		displayPortViewHighlight(context.drawingView, connPointAbs, highlight = DrawModule.properties.get(PortView.PROP_CONNECT_DENY))

		// Layout EdgeView
		val direction = draggedEndpointType.getDirectionForPortView(targetPortView!!)
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = connPointAbs,
			origDirs = null,
			destDirs = setOf(direction))

		// Don't layout EdgeView
	}

	protected fun isLastUndoAfterRemovingLastPoint(): Boolean {
		if (adjustment!!.model.size == 1) {
			return true
		}

		val currentLocation = draggedEndpointType.getLocation(edgeView!!)
		draggedEndpointType.remove(edgeView!!)

		adjustment!!.model.undo()
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = currentLocation,
			null,
			null)
		edgeView!!.validate()

		return false
	}

	/**
	 * Creates the [EdgeView] to be used for connecting and adds it to the [Drawing].
	 * Removes the [PortView] highlight.
	 *
	 * @param view the [DrawingView] to which the created [EdgeView] is added
	 * @param startPoint the [Point2D] at which the created [EdgeView] starts.
	 * @param netView the [NetView] of the [EdgeView] to be created
	 */
	protected fun createEdgeView(view: DrawingView<GraphElementView<*>, GraphView>, startPoint: Point2D, netView: NetView<Any>?) {
		(if (netView == null) edgeViewFactory.createEdgeView(view.drawing) else edgeViewFactory.createEdgeView(view.drawing, netView)).also { ev ->
			this.edgeView = ev
			ev.underConstruction = true

			ev.addSegmentPoint(startPoint)
			ev.addSegmentPoint(startPoint)

			view.drawing.add(ev)
			view.selectionManager.deselectAll()
			view.selectionManager.select(ev)
		}
	}

	protected open fun cancel(editor: Editor) {
		LOG.userTrail("Creation of EdgeView cancelled")
		edgeView?.let {
			it.unconnectFromOrigin()
			it.unconnectFromDestination()
			editor.view.drawing.remove(it)
			ConnectionPointHighlighter.removePortViewHighlight()
			reset()
		}
	}
}