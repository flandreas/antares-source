package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter.displayPortViewHighlight
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * A base class of connectors that support dragging an endpoint of an [EdgeView] around.
 */
abstract class AbstractConnector(
	protected val draggedEndpointType: EdgeViewEndpointType
) {

	abstract val handler : StateMachineInputEventHandler<EditInputEventContext>

	/** The [EdgeView] whose endpoint is being dragged, `null` before mouse has been pressed */
	protected var edgeView: EdgeView<Any>? = null

	/** The found target [PortView], if any. */
	var targetPortView: PortView<*>? = null

	/** The found target [EdgeView], if any. */
	protected var targetEdgeView: EdgeView<*>? = null

	protected var targetEdgeViewSegmentIndex: Int? = null

	protected open fun reset() {
		edgeView = null
		targetPortView = null
		targetEdgeView = null
		targetEdgeViewSegmentIndex = null
	}

	protected fun displayPortViewHighlight(context: EditInputEventContext, location: Point2D, alternativeView: Boolean = false) {
		displayPortViewHighlight(context.drawingView, location, alternativeView)
		context.view.setCursor(Cursor.CROSSHAIR)
	}

	protected fun removePortViewHighlight(context: EditInputEventContext) {
		ConnectionPointHighlighter.removePortViewHighlight()
		context.editor.snapManager.done()
	}

	protected fun moveEdgeViewEndpoint(context: EditInputEventContext) {
		if (targetPortView == null) {
			val snap = context.editor.snapManager.snap(context.x, context.y)
			draggedEndpointType.moveTo(edgeView!!, context.location.add(snap))
			edgeView?.validate()
		}
	}

	protected fun insideTargetPortView(type: EdgeViewEndpointType, context: EditInputEventContext): Boolean {
		val destVerticeView = context.drawingView.drawing.getDrawable { it.contains(context.location) && it !== edgeView }
		if (destVerticeView == null || destVerticeView !is VerticeView<*>) {
			clearTargetPortView()
			return false
		}

		val pv = (destVerticeView).getPortViewAtConnectionPoint(context.x, context.y)
		if (pv == null || pv.port.isConnected || !pv.connectable || !type.canConnectTo(pv.port, edgeView!!.net!!, context.drawingView.drawing as GraphView)) {
			clearTargetPortView()
			return false
		}

		targetPortView = pv

		return true
	}

	protected fun snapToTargetPortView(context: EditInputEventContext) {
		// Start highlighting current destination PortView
		val connPointAbs = targetPortView!!.owner!!.getPortConnectionPoint(targetPortView!!.port)
		displayPortViewHighlight(context.drawingView, connPointAbs)

		// Snap EdgeView end to connection point
		draggedEndpointType.moveTo(edgeView!!, Point2D(connPointAbs.x, connPointAbs.y))

		// Layout EdgeView
		val direction = draggedEndpointType.getDirectionForPortView(targetPortView!!)
		draggedEndpointType.layout(edgeView!!, direction)

		edgeView?.validate()
	}

	protected fun clearTargetPortView() {
		targetPortView = null
	}

	protected fun insideTargetEdgeView(type: EdgeViewEndpointType, context: EditInputEventContext): Boolean {
		val destDrawable = context.drawingView.drawing.getDrawable { it !== edgeView && it.contains(context.location) }
		if (destDrawable == null || destDrawable !is EdgeView<*> || !canConnectTo(type, destDrawable, context.drawingView.drawing as GraphView)) {
			clearTargetEdgeView()
			return false
		}

		clearTargetPortView()
		targetEdgeView = destDrawable

		return true
	}

	protected open fun canConnectTo(type: EdgeViewEndpointType, edgeView: EdgeView<out Any>, graphView: GraphView): Boolean {
		return true
	}

	protected fun snapToTargetEdgeView(context: EditInputEventContext) {
		targetEdgeView!!.snap(context.x, context.y, context.editor.snapManager)?.let { snapResult ->
			targetEdgeViewSegmentIndex = snapResult.segmentIndex
			displayPortViewHighlight(context.drawingView, snapResult.location)
			draggedEndpointType.moveTo(edgeView!!, snapResult.location)
			draggedEndpointType.layout(edgeView!!, null)
			edgeView!!.layout
		}
	}

	private fun clearTargetEdgeView() {
		targetEdgeView = null
	}
}