package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.base.logger


/**
 * An [InputEventHandler] that connects an [OutputPort] of a [VerticeView] with an [InputPort]
 * of a [VerticeView], or leaves the [EdgeView] open-ended.
 */
class OutputToInputConnector(
	private val connectServiceSupplier: () -> GraphViewConnectService,
	edgeViewFactorySupplier: () -> EdgeViewFactory<Any>
) : AbstractCreateEdgeViewConnector(
	portTypeCond = { it.isOutput },
	edgeViewFactorySupplier = edgeViewFactorySupplier,
	endpointType = EdgeViewEndpointType.DESTINATION
) {

	companion object {
		private val LOG by logger(OutputToInputConnector::class)
	}

	/** ---- [InputEventHandler] */

	override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		LOG.debug("mouseReleased at (${context.x},${context.y})")
		super.mouseReleased(context)

		if (isValidEdgeView()) {
			completeConnectingToEndPort(context)
		} else {
			cancel(context.editor)
		}

		return null
	}

	/** ---- [AbstractCreateEdgeViewConnector] */

	override fun connectEdgeViewToStartPort() {
		edgeView!!.connectToOrigin(startPortView!!.owner, startPortView!!.port as Port<Any>)
	}

	/** --- [OutputToInputConnector] */

	private fun completeConnectingToEndPort(context: EditInputEventContext) {
		context.drawingView().drawing.remove(edgeView!!)

		val endPortView = getEndpointHandler().targetPortView
		context.editor.commandManager.execute(
			ConnectCommand(
				editor = context.editor,
				connectService = connectServiceSupplier.invoke(),
				edgeView = edgeView!!,
				origConnectableView = startPortView!!.owner,
				origPort = startPortView!!.port,
				destConnectableView = endPortView?.owner,
				destPort = endPortView?.port))
		context.drawingView().selectionManager.select(edgeView!!)

		edgeView = null
	}
}