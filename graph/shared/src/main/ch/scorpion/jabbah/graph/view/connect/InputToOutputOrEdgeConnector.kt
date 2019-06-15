package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * An [InputEventHandler] that connects an [InputPort] of a [VerticeView] with an [OutputPort] of a [VerticeView],
 * or with an [EdgeView], or that leaves the [EdgeView] open.
 *
 * Designed as a single instance being used by multiple [VerticeView]s. Therefore, determine the [VerticeView] on which
 * this [InputToOutputOrEdgeConnector] operates by calling [useFor] before every usage.
 *
 * TODO Refactor: A lot of code common with [OutputToInputConnector].
 */
class InputToOutputOrEdgeConnector(
	private val connectServiceSupplier: () -> GraphViewConnectService,
	edgeViewFactorySupplier: () -> EdgeViewFactory<Any>
) : AbstractCreateEdgeViewConnector(edgeViewFactorySupplier, EdgeViewEndpointType.ORIGIN, allowEdgeViewAsTarget = true) {

	companion object {
		private val LOG by logger(InputToOutputOrEdgeConnector::class)
	}

	/** The [VerticeView] where the new connection ends. */
	private var verticeView: VerticeView<*>? = null

	/** The [PortView] in [verticeView] where the new connection ends.  */
	private var destPortView: PortView<*>? = null

	/** Prepares this [InputToOutputOrEdgeConnector] to be used to created [EdgeView]s that ends in the specified [VerticeView].*/
	fun useFor(verticeView: VerticeView<*>) {
		this.verticeView = verticeView
	}

	/** ---- [InputEventHandler] */

	override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (isOnInputPort(context)) {
			return this
		}
		exitDestinationPortViewIfNecessary(context)
		return null
	}

	private fun isOnInputPort(context: EditInputEventContext): Boolean {
		if (verticeView!!.contains(context.x, context.y)) {
			val pv = verticeView!!.getPortViewAtConnectionPoint(context.x, context.y)
			if (pv != null && !pv.port.isConnected && pv.port.portType.isInput) {
				destPortView = pv
				if (portViewHighlight == null) {
					val connPoint = verticeView!!.getPortConnectionPoint(destPortView!!.port)
					displayPortViewHighlight(context.drawingView(), Point2D(connPoint))
				}
				return true
			}
		}
		return false
	}

	private fun exitDestinationPortViewIfNecessary(context: EditInputEventContext) {
		if (portViewHighlight != null) {
			removePortViewHighlight(context.drawingView())
			destPortView = null
		}
	}

	override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (portViewHighlight == null) {
			return null
		}

		createEdgeView(context.drawingView(), verticeView!!.getPortConnectionPoint(destPortView!!.port), null)
		getEndpointHandler().useFor(edgeView!!)
		removePortViewHighlight(context.drawingView())

		edgeView!!.model!!.connect(destPortView!!.port as Port<Any>)
		edgeView!!.connectToDestination(destPortView!!.owner, destPortView!!.port as Port<Any>)

		return this
	}

	override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (LOG.isTraceEnabled()) {
			LOG.trace("mouseDragged to (${context.x},${context.y})")
		}
		// Forward to DragEdgeViewEndpointHandler, but keep control in order to handle mouseReleased
		if (edgeView != null) {
			super.mouseDragged(context)
		}
		return this
	}

	override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		LOG.debug("mouseReleased at (${context.x},${context.y})")
		super.mouseReleased(context)

		if (isValidEdgeView()) {
			if (getEndpointHandler().targetPortView != null) {
				completeConnectingToOutput(context)
			} else if (getEndpointHandler().targetEdgeView != null) {
				completeConnectingToEdge(context)
			}
		} else {
			cancel(context.editor)
		}

		return null
	}

	/** ---- [InputToOutputOrEdgeConnector] */

	private fun getEndpointHandler(): DragEdgeViewEndpointHandler {
		return successor as DragEdgeViewEndpointHandler
	}

	private fun completeConnectingToOutput(context: EditInputEventContext) {
		context.drawingView().drawing.remove(edgeView!!)

		val origPortView = getEndpointHandler().targetPortView
		context.editor.commandManager.execute(
			ConnectCommand(
				editor = context.editor,
				connectService = connectServiceSupplier.invoke(),
				edgeView = edgeView!!,
				origConnectableView = origPortView?.owner,
				origPort = origPortView?.port,
				destConnectableView = destPortView?.owner,
				destPort = destPortView?.port))
		context.drawingView().selectionManager.select(edgeView!!)

		edgeView = null
	}

	private fun completeConnectingToEdge(context: EditInputEventContext) {
		context.drawingView().drawing.remove(edgeView!!)

		context.editor.commandManager.execute(
			SplitEdgeViewCommand(
				editor = context.editor,
				connectService = connectServiceSupplier.invoke(),
				graphView = context.editor.drawing as GraphView<GraphElementView<*>>,
				origEdgeView = getEndpointHandler().targetEdgeView!!,
				segmentIndex = getEndpointHandler().targetEdgeViewSegmentIndex!!,
				newEdgeView = edgeView!!,
				targetPortView = destPortView!!,
				nodeView = null
			)
		)
	}
}