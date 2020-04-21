package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.Connection
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

	override fun createAdjustment(): EdgeViewAdjustmentView {
		return SimpleEdgeViewAdjustmentView.forOriginAdjustmentOf(edgeView!!)
	}

	override fun connectEdgeViewToStartPort() {
		edgeView!!.connectToDestination(Connection(startPortView!!.owner!!, startPortView!!.port as Port<Any>))
		edgeView!!.layout.layoutDestination()
	}

	override fun completeConnectingToEndPortOrOpen(context: EditInputEventContext) {
		context.drawingView().drawing.remove(edgeView!!)

		val connectOriginCommand = if (targetPortView != null) {
			ConnectOriginCommand(context.editor, connectService, edgeView!!, targetPortView!!.owner!!, targetPortView!!.port)
		} else {
			null
		}

		val connectDestinationCommand = if (startPortView != null) {
			ConnectDestinationCommand(context.editor, connectService, edgeView!!, startPortView!!.owner!!, startPortView!!.port)
		} else {
			null
		}

		context.editor.commandManager.execute(
			ConnectCommand(
				context.editor,
				edgeView!!,
				connectOriginCommand,
				connectDestinationCommand
			)
		)

		context.drawingView().selectionManager.select(edgeView!!)
	}
}