package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.editor.AddCommand
import io.antarescircuit.jabbah.graph.model.InputPort
import io.antarescircuit.jabbah.graph.model.OutputPort
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.Connection
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewFactory

/**
 * A connector that connects an [InputPort] of a [VerticeView] with an [OutputPort] of a [VerticeView],
 * or with an [EdgeView], or that leaves the created [EdgeView] open-ended.
 */
class InputToOutputOrEdgeConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	edgeViewFactory: EdgeViewFactory = GraphViewModule.getEdgeViewFactory()
) : AbstractPortViewStartConnector(
	portTypeCond = { it.isInput },
	connectService = connectService,
	edgeViewFactory = edgeViewFactory,
	draggedEndpointType = EdgeViewEndpointType.ORIGIN,
	allowEdgeViewAsTarget = true
) {

	companion object {
		private val LOG by logger(InputToOutputOrEdgeConnector::class)
	}

	override fun createAdjustment(): EdgeViewAdjustmentView {
		return SimpleEdgeViewAdjustmentView.forOriginAdjustmentOf(edgeView!!)
	}

	override fun connectEdgeViewToStartPort() {
		edgeView!!.connectToDestination(Connection(startPortView!!.owner!!, startPortView!!.port as Port<Any>))
		edgeView!!.layout.layoutDestination()
	}

	private fun logConnect() {
		if (targetPortView != null) {
			LOG.userTrail("Connect input ${startPortView?.port?.portId} of ${startPortView?.owner?.id} with output ${targetPortView?.port?.portId} of ${targetPortView?.owner?.id}")
		} else {
			LOG.userTrail("Connect input ${startPortView?.port?.portId} of ${startPortView?.owner?.id} open-ended at ${edgeView?.polyline?.getFirstPoint()}")
		}
	}

	override fun completeConnectingToEndPortOrOpen(context: EditInputEventContext) {
		logConnect()

		connectService.unconnect(edgeView!!)
		context.drawingView.drawing.remove(edgeView!!)

		try {
			context.editor.commandManager.beginTransaction("graph.command.connect", context.drawingView)

			val addCommand = AddCommand(context.editor, edgeView!!)
			context.editor.commandManager.execute(addCommand)

			if (targetPortView != null) {
				context.editor.commandManager.execute(
					ConnectOriginCommand(
						context.editor,
						connectService,
						addCommand.addedComponentId,
						targetPortView!!.owner!!.id,
						targetPortView!!.port.portId
					)
				)
			} else {
				context.editor.commandManager.execute(
					MoveOriginEndpointCommand(
						context.editor,
						addCommand.addedComponentId,
						startPortView!!.location,
						edgeView!!.polyline.getFirstPoint()
					)
				)
			}

			context.editor.commandManager.execute(
				ConnectDestinationCommand(
					context.editor,
					connectService,
					addCommand.addedComponentId,
					startPortView!!.owner!!.id,
					startPortView!!.port.portId
				)
			)

			GraphViewModule.connectionEstablishedHandler?.handle(context.editor, startPortView!!.port)?.let {
				context.editor.commandManager.execute(it)
			}

			context.editor.commandManager.commitTransaction()

			context.drawingView.selectionManager.select(
				context.drawingView.drawing.getWithId(addCommand.addedComponentId)!!
			)
		} catch (e: Exception) {
			if (context.editor.commandManager.isInTransaction) {
				context.editor.commandManager.rollbackTransaction()
			}
			postConnectorErrorMessage(e)
		}
	}
}