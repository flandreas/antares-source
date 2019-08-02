package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * An [InputEventHandler] that connects an [InputPort] of a [VerticeView] with an [OutputPort] of a [VerticeView],
 * or with an [EdgeView], or that leaves the [EdgeView] open.
 */
class InputToOutputOrEdgeConnector(
	private val connectServiceSupplier: () -> GraphViewConnectService,
	edgeViewFactorySupplier: () -> EdgeViewFactory<Any>
) : AbstractCreateEdgeViewConnector(
	portTypeCond = { it.isInput },
	edgeViewFactorySupplier = edgeViewFactorySupplier,
	endpointType = EdgeViewEndpointType.ORIGIN,
	allowEdgeViewAsTarget = true
) {

	companion object {
		private val LOG by logger(InputToOutputOrEdgeConnector::class)
	}

	/** ---- [InputEventHandler] */

	override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		LOG.debug("mouseReleased at (${context.x},${context.y})")
		super.mouseReleased(context)

		if (isValidEdgeView()) {
			if (getEndpointHandler().targetPortView != null) {
				completeConnectingToEndPort(context)
			} else if (getEndpointHandler().targetEdgeView != null) {
				completeConnectingToEdge(context)
			}
		} else {
			cancel(context.editor)
		}

		return null
	}

	/** ---- [AbstractCreateEdgeViewConnector] */

	override fun connectEdgeViewToStartPort() {
		edgeView!!.connectToDestination(startPortView!!.owner, startPortView!!.port as Port<Any>)
	}

	/** ---- [InputToOutputOrEdgeConnector] */

	private fun completeConnectingToEndPort(context: EditInputEventContext) {
		context.drawingView().drawing.remove(edgeView!!)

		val endPortView = getEndpointHandler().targetPortView
		context.editor.commandManager.execute(
			ConnectCommand(
				editor = context.editor,
				connectService = connectServiceSupplier.invoke(),
				edgeView = edgeView!!,
				origConnectableView = endPortView?.owner,
				origPort = endPortView?.port,
				destConnectableView = startPortView?.owner,
				destPort = startPortView?.port))
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
				targetPortView = startPortView!!,
				nodeView = null
			)
		)
	}
}