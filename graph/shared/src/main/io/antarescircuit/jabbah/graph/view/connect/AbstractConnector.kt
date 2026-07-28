package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.StateMachineInputEventHandler
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter.displayPortViewHighlight
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType.ORIGIN
import io.antarescircuit.jabbah.graph.view.port.PortView

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

	protected fun insideDenyingPortView(type: EdgeViewEndpointType, context: EditInputEventContext): Boolean {
		val destVerticeView = context.drawingView.drawing.getDrawable { it.contains(context.location) && it !== edgeView }
		if (destVerticeView == null || destVerticeView !is VerticeView<*>) {
			clearTargetPortView()
			return false
		}

		val pv = (destVerticeView).getPortViewAtConnectionPoint(context.x, context.y)
		if (pv == null || pv.port.isConnected || type.canConnectTo(pv.port, edgeView!!.net!!, context.drawingView.drawing as GraphView)) {
			clearTargetPortView()
			return false
		}

		targetPortView = pv

		type.canConnectTo(pv.port, edgeView!!.net!!, context.drawingView.drawing as GraphView)

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
		draggedEndpointType.layout(edgeView!!, setOf(direction))

		edgeView?.validate()
	}

	protected fun snapToDenyingPortView(context: EditInputEventContext) {
		val connPointAbs = targetPortView!!.owner!!.getPortConnectionPoint(targetPortView!!.port)
		displayPortViewHighlight(context.drawingView, connPointAbs, highlight = DrawModule.properties.get(PortView.PROP_CONNECT_DENY))

		// Snap EdgeView end to connection point
		draggedEndpointType.moveTo(edgeView!!, Point2D(connPointAbs.x, connPointAbs.y))

		// Don't layout EdgeView
	}

	protected fun clearTargetPortView() {
		targetPortView = null
	}

	protected fun insideTargetEdgeView(type: EdgeViewEndpointType, context: EditInputEventContext): Boolean {
		val destEdgeView = context.drawingView.drawing.getDrawable { it is EdgeView<*> && it.model !== edgeView?.model && it.contains(context.location) }
		if (destEdgeView == null || !canConnectTo(type, destEdgeView as EdgeView<*>, context.drawingView.drawing as GraphView)) {
			clearTargetEdgeView()
			return false
		}

		clearTargetPortView()
		targetEdgeView = destEdgeView

		return true
	}

	protected fun insideCurrentTargetEdgeView(context: EditInputEventContext): Boolean {
		if (targetEdgeView == null) {
			clearTargetEdgeView()
			return false
		}

		return targetEdgeView!!.contains(context.location)
	}

	protected fun insideTargetEdgeView(context: EditInputEventContext): Boolean {
		val destEdgeView = context.drawingView.drawing.getDrawable { it is EdgeView<*> && it.model !== edgeView?.model && it.contains(context.location) }
		if (destEdgeView == null || !canConnectTo(destEdgeView as EdgeView<*>, context.drawingView.drawing as GraphView)) {
			clearTargetEdgeView()
			return false
		}

		clearTargetPortView()
		targetEdgeView = destEdgeView

		return true
	}

	protected open fun canConnectTo(type: EdgeViewEndpointType, edgeView: EdgeView<out Any>, graphView: GraphView): Boolean {
		return true
	}

	private fun canConnectTo(edgeView: EdgeView<out Any>, graphView: GraphView): Boolean {
		return this.edgeView?.net?.canConnectTo(edgeView.net!!, graphView) ?: false
	}

	protected fun snapToTargetEdgeView(context: EditInputEventContext) {
		targetEdgeView!!.snap(context.x, context.y, draggedEndpointType == ORIGIN, context.editor.snapManager)?.let { snapResult ->
			targetEdgeViewSegmentIndex = snapResult.segmentIndex
			displayPortViewHighlight(context.drawingView, snapResult.location)
			draggedEndpointType.moveTo(edgeView!!, snapResult.location)
			draggedEndpointType.layout(edgeView!!, snapResult.directions)
			edgeView!!.layout
		}
	}

	protected fun insideDenyingEdgeView(type: EdgeViewEndpointType, context: EditInputEventContext): Boolean {
		val destEdgeView = context.drawingView.drawing.getDrawable { it is EdgeView<*> && it.model !== edgeView?.model && it.contains(context.location) }
		if (destEdgeView == null || canConnectTo(type, destEdgeView as EdgeView<*>, context.drawingView.drawing as GraphView)) {
			clearTargetEdgeView()
			return false
		}

		clearTargetPortView()
		targetEdgeView = destEdgeView

		return true
	}

	protected fun insideDenyingEdgeView(context: EditInputEventContext): Boolean {
		val destEdgeView = context.drawingView.drawing.getDrawable { it is EdgeView<*> && it.model !== edgeView?.model && it.contains(context.location) }
		if (destEdgeView == null || canConnectTo(destEdgeView as EdgeView<*>, context.drawingView.drawing as GraphView)) {
			clearTargetEdgeView()
			return false
		}
		clearTargetPortView()
		targetEdgeView = destEdgeView
		return true
	}

	protected fun snapToDenyingEdgeView(context: EditInputEventContext) {
		targetEdgeView!!.snap(context.x, context.y, draggedEndpointType == ORIGIN, context.editor.snapManager)?.let { snapResult ->
			targetEdgeViewSegmentIndex = snapResult.segmentIndex
			displayPortViewHighlight(context.drawingView, snapResult.location, highlight = DrawModule.properties.get(PortView.PROP_CONNECT_DENY))
			draggedEndpointType.moveTo(edgeView!!, snapResult.location)
			// Don't lay out EdgeView
		}
	}

	private fun clearTargetEdgeView() {
		targetEdgeView = null
	}

	protected fun postConnectorErrorMessage(e: Exception) {
		BaseModule.eventBus.post(
			ComponentMessage(
				ComponentMessageType.Error,
				null,
				"graph.tool.connector.error.text",
				e.message ?: e::class.simpleName
			)
		)
	}
}