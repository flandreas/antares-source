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
 * A connector that connects an [OutputPort] of a [VerticeView] with an [InputPort]
 * of a [VerticeView], or that leaves the created [EdgeView] open-ended.
 */
class OutputToInputConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	edgeViewFactory: EdgeViewFactory<Any> = GraphViewModule.getEdgeViewFactory()
) : AbstractPortViewStartConnector(
	portTypeCond = { it.isOutput },
	connectService = connectService,
	edgeViewFactory = edgeViewFactory,
	draggedEndpointType = EdgeViewEndpointType.DESTINATION
) {

	override fun connectEdgeViewToStartPort() {
		edgeView!!.connectToOrigin(startPortView!!.owner, startPortView!!.port as Port<Any>)
	}

	override fun completeConnectingToEndPort(context: EditInputEventContext) {
		context.drawingView().drawing.remove(edgeView!!)

		context.editor.commandManager.execute(
			ConnectCommand(
				editor = context.editor,
				connectService = connectService,
				edgeView = edgeView!!,
				origConnectableView = startPortView!!.owner,
				origPort = startPortView!!.port,
				destConnectableView = targetPortView?.owner,
				destPort = targetPortView?.port))
		context.drawingView().selectionManager.select(edgeView!!)
	}
}