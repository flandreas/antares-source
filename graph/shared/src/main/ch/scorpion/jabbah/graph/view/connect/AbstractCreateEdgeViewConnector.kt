package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
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
	protected var adjustment: EdgeViewAdjustmentView? = null
		private set

	protected abstract fun createAdjustment(): EdgeViewAdjustmentView

	override fun reset() {
		super.reset()
		adjustment = null
	}

	protected fun beginAdjustment(context: EditInputEventContext) {
		adjustment = createAdjustment()
		context.drawingView.animationContainer.add(adjustment!!)
		context.drawingView.animationContainer.validate()
	}

	protected fun endAdjustment(context: EditInputEventContext) {
		context.drawingView.animationContainer.remove(adjustment!!)
	}

	protected open fun getMoveAdjustedPointOrigDirs(layoutIndex: Int): Set<Direction>? {
		if (layoutIndex > 0) {
			// The last segment should preferably be orthogonal to the second-to-last one
			return edgeView!!.layout.type.getSegmentDirection(edgeView!!, layoutIndex - 1)?.orthogonalSet()
		}
		return null
	}

	protected fun moveAdjustedPoint(context: EditInputEventContext) {
		val p = context.location.add(context.editor.snapManager.snap(context.x, context.y))
		val layoutIndex = adjustment!!.model.current
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = layoutIndex,
			location = p,
			origDirs = getMoveAdjustedPointOrigDirs(layoutIndex),
			destDir = null)
	}

	protected fun addAdjustedPoint(context: EditInputEventContext) {
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = context.location.add(context.editor.snapManager.snap(context.x, context.y)),
			null
		)

		adjustment!!.model.add()
		edgeView!!.validate()
	}

	protected fun adjustToTargetPortView(context: EditInputEventContext) {
		// Start highlighting current destination PortView
		val connPointAbs = targetPortView!!.owner!!.getPortConnectionPoint(targetPortView!!.port)
		ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView, connPointAbs)

		// Layout EdgeView
		val direction = draggedEndpointType.getDirectionForPortView(targetPortView!!)
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = connPointAbs,
			origDirs = null,
			destDir = direction)

		edgeView?.validate()
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