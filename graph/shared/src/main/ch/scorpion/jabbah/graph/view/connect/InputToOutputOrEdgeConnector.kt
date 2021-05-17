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
		connectService.unconnect(edgeView!!)
		context.drawingView().drawing.remove(edgeView!!)

		context.editor.commandManager.beginTransaction("graph.command.connect", context.drawingView())

		val addCommand = AddCommand(context.editor, edgeView!!)
		context.editor.commandManager.execute(addCommand)

		if (targetPortView != null) {
			context.editor.commandManager.execute(
				ConnectOriginCommand(
					context.editor,
					connectService,
					addCommand.addedComponentId,
					targetPortView!!.owner!!.id,
					targetPortView!!.port.portId))
		} else {
			context.editor.commandManager.execute(
				MoveOriginEndpointCommand(
					context.editor,
					addCommand.addedComponentId,
					startPortView!!.location,
					edgeView!!.polyline.getFirstPoint()))
		}

		context.editor.commandManager.execute(
			ConnectDestinationCommand(
				context.editor,
				connectService,
				addCommand.addedComponentId,
				startPortView!!.owner!!.id,
				startPortView!!.port.portId)
		)

		context.editor.commandManager.commitTransaction()

		context.drawingView().selectionManager.select(
			context.drawingView().drawing.getWithId(addCommand.addedComponentId)!!)
	}
}