package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory

/**
 * A connector that connects an [InputPort] of a [VerticeView] with an [OutputPort] of a [VerticeView],
 * or with an [EdgeView], or that leaves the created [EdgeView] open-ended.
 */
class InputToOutputOrEdgeConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	edgeViewFactory: EdgeViewFactory<Any> = GraphViewModule.getEdgeViewFactory()
) : AbstractPortViewStartConnector(
	portTypeCond = { it.isInput },
	connectService = connectService,
	edgeViewFactory = edgeViewFactory,
	draggedEndpointType = EdgeViewEndpointType.ORIGIN,
	allowEdgeViewAsTarget = true
) {

	override fun connectEdgeViewToStartPort() {
		edgeView!!.connectToDestination(startPortView!!.owner, startPortView!!.port as Port<Any>)
	}

	override fun completeConnectingToEndPort(context: EditInputEventContext) {
		context.drawingView().drawing.remove(edgeView!!)

		context.editor.commandManager.execute(
			ConnectCommand(
				editor = context.editor,
				connectService = connectService,
				edgeView = edgeView!!,
				origConnectableView = targetPortView?.owner,
				origPort = targetPortView?.port,
				destConnectableView = startPortView?.owner,
				destPort = startPortView?.port))
		context.drawingView().selectionManager.select(edgeView!!)
	}
}