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

	override fun createAdjustment(): EdgeViewAdjustmentView {
		return SimpleEdgeViewAdjustmentView.forDestinationAdjustmentOf(edgeView!!)
	}

	override fun connectEdgeViewToStartPort() {
		edgeView!!.connectToOrigin(Connection(startPortView!!.owner!!, startPortView!!.port as Port<Any>))
		// Adapt to PortView that might has reduced its length
		edgeView!!.layout.layoutOrigin()
	}

	override fun completeConnectingToEndPortOrOpen(context: EditInputEventContext) {
		context.drawingView().drawing.remove(edgeView!!)

		val connectOriginCommand = if (startPortView != null) {
			ConnectOriginCommand(context.editor, connectService, edgeView!!, startPortView!!.owner!!, startPortView!!.port)
		} else {
			null
		}

		val connectDestinationCommand = if (targetPortView != null) {
			ConnectDestinationCommand(context.editor, connectService, edgeView!!, targetPortView!!.owner!!, targetPortView!!.port)
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