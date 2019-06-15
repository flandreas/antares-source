package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger

/**
 * Controls dragging an endpoint of an [EdgeView] towards an target [PortView] of a [VerticeView],
 * which can be either an input or an output.
 *
 * Designed as a single instance being used by multiple [EdgeView]s. Therefore, determine the [EdgeView] on which
 * this [DragEdgeViewEndpointHandler] operates by calling [useFor] before every usage.
 */
class DragEdgeViewEndpointHandler(
	val edgeViewEndpointType: EdgeViewEndpointType,
	private val allowEdgeViewAsTarget: Boolean = false
) : AbstractConnectionPointHighlighter() {

	companion object {
		private val LOG by logger(DragEdgeViewEndpointHandler::class)
	}

	/** The [EdgeView] whose endpoint is being dragged. Set in [useFor]. */
	private var edgeView: EdgeView<*>? = null

	/** The found target [PortView], if any. */
	var targetPortView: PortView<*>? = null

	/** The found target [EdgeView], if any. */
	var targetEdgeView: EdgeView<*>? = null

	var targetEdgeViewSegmentIndex: Int? = null

	/** ---- [InputEventHandler] */

	override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (LOG.isTraceEnabled()) {
			LOG.trace("mouseDragged to (${context.x},${context.y})")
		}

		moveEdgeViewEndpoint(context)

		if (isOnTargetPortView(context)) {
			snapToTargetPortView(context)
		} else if (allowEdgeViewAsTarget && isOnTargetEdgeView(context)) {
			snapToTargetEdgeView(context)
		}

		return this
	}

	private fun moveEdgeViewEndpoint(context: EditInputEventContext) {
		if (targetPortView == null) {
			val snap = context.editor.snapManager.snap(context.x, context.y)
			edgeViewEndpointType.moveTo(edgeView!!, Point2D(context.x + snap.x, context.y + snap.y))
			edgeView?.validate()
		}
	}

	private fun isOnTargetPortView(context: EditInputEventContext): Boolean {
		val destVerticeView = context.drawingView().drawing.getDrawable { it.contains(context.location) && it !== edgeView }
		if (destVerticeView == null || destVerticeView !is VerticeView<*>) {
			exitTargetPortView(context.drawingView())
			return false
		}

		val pv = (destVerticeView).getPortViewAtConnectionPoint(context.x, context.y)
		if (pv == null || pv.port.isConnected || !pv.connectable || !edgeViewEndpointType.canConnectTo(pv.port.portType)) {
			exitTargetPortView(context.drawingView())
			return false
		}

		targetPortView = pv
		targetEdgeView = null

		return true
	}

	private fun isOnTargetEdgeView(context: EditInputEventContext): Boolean {
		val destDrawable = context.drawingView().drawing.getDrawable { it !== edgeView && it.contains(context.location)}
		if (destDrawable == null || destDrawable !is EdgeView<*>) {
			exitTargetEdgeView(context.drawingView())
			return false
		}

		targetPortView = null
		targetEdgeView = destDrawable

		return true
	}

	private fun snapToTargetPortView(context: EditInputEventContext) {
		if (portViewHighlight == null) {
			// Start highlighting current destination PortView
			val connPointAbs = targetPortView!!.owner!!.getPortConnectionPoint(targetPortView!!.port)
			displayPortViewHighlight(context.drawingView(), connPointAbs)

			// Snap EdgeView end to connection point
			edgeViewEndpointType.moveTo(edgeView!!, Point2D(connPointAbs.x, connPointAbs.y))

			// Layout EdgeView
			val direction = edgeViewEndpointType.getDirectionForPortView(targetPortView!!)

			edgeViewEndpointType.layout(edgeView!!, direction)
			edgeView?.validate()
		}
	}

	private fun snapToTargetEdgeView(context: EditInputEventContext) {
		if (portViewHighlight == null) {
			targetEdgeView!!.snap(context.x, context.y, context.editor.view.grid)?.let { snapResult ->
				targetEdgeViewSegmentIndex = snapResult.segmentIndex
				displayPortViewHighlight(context.drawingView(), snapResult.location)
				edgeViewEndpointType.moveTo(edgeView!!, snapResult.location)
				edgeViewEndpointType.layout(edgeView!!, null)
				edgeView!!.layout
			}
		}
	}

	override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		LOG.debug("mouseReleased")
		removePortViewHighlight(context.drawingView())
		return null
	}

	/** ---- [DragEdgeViewEndpointHandler] */

	/** Binds the specified [EdgeView] in order to be used by this [DragEdgeViewEndpointHandler].*/
	fun useFor(edgeView: EdgeView<*>) {
		this.edgeView = edgeView
	}

	private fun exitTargetPortView(view: DrawingView<*>) {
		removePortViewHighlight(view)
		targetPortView = null
	}

	private fun exitTargetEdgeView(view: DrawingView<*>) {
		removePortViewHighlight(view)
		targetEdgeView = null
	}
}