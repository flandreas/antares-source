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
 * A connector that connects an [OutputPort] of a [VerticeView] with an [InputPort] of a [VerticeView],
 * or with an [EdgeView], or that leaves the created [EdgeView] open-ended.
 */
class OutputToInputOrEdgeConnector(
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	edgeViewFactory: EdgeViewFactory = GraphViewModule.getEdgeViewFactory()
) : AbstractPortViewStartConnector(
	portTypeCond = { it.isOutput },
	connectService = connectService,
	edgeViewFactory = edgeViewFactory,
	draggedEndpointType = EdgeViewEndpointType.DESTINATION,
	allowEdgeViewAsTarget = true
) {

	companion object {
		private val LOG by logger(OutputToInputOrEdgeConnector::class)
	}

	override fun createAdjustment(): EdgeViewAdjustmentView =
		SimpleEdgeViewAdjustmentView.forDestinationAdjustmentOf(edgeView!!)

	override fun connectEdgeViewToStartPort() {
		@Suppress("UNCHECKED_CAST")
		edgeView!!.connectToOrigin(Connection(startPortView!!.owner!!, startPortView!!.port as Port<Any>))
		// Adapt to PortView that might have reduced its length
		edgeView!!.layout.layoutOrigin()
	}

	private fun logConnect() {
		if (targetPortView != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Connect from ${startPortView?.owner?.getUnconnectedPortConnectionPoint(startPortView!!.port)} to ${targetPortView?.owner?.getUnconnectedPortConnectionPoint(targetPortView!!.port)}")
			}
			LOG.userTrail("Connect output ${startPortView?.port?.portId} of ${startPortView?.owner?.type} ${startPortView?.owner?.id} with input ${targetPortView?.port?.portId} of ${targetPortView?.owner?.type} ${targetPortView?.owner?.id}")
		} else {
			LOG.userTrail("Connect output of ${startPortView?.owner?.type} ${startPortView?.owner?.id} open-ended at ${edgeView?.polyline?.getLastPoint()}")
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

			context.editor.commandManager.execute(
				ConnectOriginCommand(
					context.editor,
					connectService,
					addCommand.addedComponentId,
					startPortView!!.owner!!.id,
					startPortView!!.port.portId
				)
			)

			if (targetPortView != null) {
				context.editor.commandManager.execute(
					ConnectDestinationCommand(
						context.editor,
						connectService,
						addCommand.addedComponentId,
						targetPortView!!.owner!!.id,
						targetPortView!!.port.portId
					)
				)
				GraphViewModule.connectionEstablishedHandler?.handle(context.editor, targetPortView!!.port)?.let {
					context.editor.commandManager.execute(it)
				}
			} else {
				context.editor.commandManager.execute(
					MoveDestinationEndpointCommand(
						context.editor,
						addCommand.addedComponentId,
						startPortView!!.location,
						edgeView!!.polyline.getLastPoint()
					)
				)
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