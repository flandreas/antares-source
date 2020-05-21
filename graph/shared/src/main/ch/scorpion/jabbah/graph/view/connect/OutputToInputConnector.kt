package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.editor.AddCommand
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
		connectService.unconnectEdgeViewOrigin(edgeView!!)
		context.drawingView().drawing.remove(edgeView!!)

		context.editor.commandManager.beginTransaction("graph.command.connect", context.drawingView())

		val addCommand = AddCommand(context.editor, edgeView!!)
		context.editor.commandManager.execute(addCommand)

		context.editor.commandManager.execute(
			ConnectOriginCommand(
				context.editor,
				connectService,
				addCommand.addedComponentId,
				startPortView!!.owner!!.id,
				startPortView!!.port.portId)
		)

		if (targetPortView != null) {
			context.editor.commandManager.execute(
				ConnectDestinationCommand(
					context.editor,
					connectService,
					addCommand.addedComponentId,
					targetPortView!!.owner!!.id,
					targetPortView!!.port.portId)
			)
		} else {
			context.editor.commandManager.execute(
				MoveDestinationEndpointCommand(
					context.editor,
					addCommand.addedComponentId,
					startPortView!!.location,
					edgeView!!.polyline.getLastPoint())
			)
		}

		context.editor.commandManager.commitTransaction()

		context.drawingView().selectionManager.select(
			context.drawingView().drawing.getWithId(addCommand.addedComponentId)!!)
	}
}