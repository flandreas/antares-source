package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter.displayPortViewHighlight
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView

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

	protected val isValidEdgeView: Boolean get() = edgeView != null && edgeView!!.isSufficientlyLarge

	/**
	 * The indices of the points in [edgeView] that have been manually set (i.e. adjusted) by the user.
	 * Organized as a [Stack] to support repetitive unrollment by pressing ESC.
	 */
	// Visible for testing
	var adjustment: EdgeViewAdjustmentView? = null
		private set

	protected abstract fun createAdjustment(): EdgeViewAdjustmentView

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
            EdgeViewEndpointType.ORIGIN -> moveAdjustedOriginPoint(context)
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
			destDir = null)
	}

	private fun moveAdjustedOriginPoint(context: EditInputEventContext) {
		val p = context.location.add(context.editor.snapManager.snap(context.x, context.y))
		val layoutIndex = adjustment!!.model.current
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = layoutIndex,
			location = p,
			origDirs = null,
			destDir = getMoveAdjustedPointDestDirs(layoutIndex, allowContinuation = false))
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
            EdgeViewEndpointType.ORIGIN -> adjustToTargetPortViewImpl(
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
			destDir = destDirs
		)
		edgeView?.validate()
	}

	protected fun adjustToTargetEdgeView(context: EditInputEventContext) {
		targetEdgeView?.snap(context.x, context.y, context.editor.snapManager)?.let { snapResult ->
			targetEdgeViewSegmentIndex = snapResult.segmentIndex
			val ownLayoutIndex = adjustment!!.model.current
			val targetDirs = targetEdgeView!!.getSegmentDirection(targetEdgeViewSegmentIndex!!)?.orthogonalSet()
			when (draggedEndpointType) {
				EdgeViewEndpointType.ORIGIN -> adjustToTargetEdgeViewImpl(
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
			destDir = destDirs
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
			destDir = setOf(direction))

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
	protected fun createEdgeView(view: DrawingView<GraphView>, startPoint: Point2D, netView: NetView<Any>?) {
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