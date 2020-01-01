package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.collection.Pair
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.DeleteCommand
import ch.scorpion.jabbah.edit.app.DrawingService
import ch.scorpion.jabbah.edit.app.DrawingServiceImpl
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.connect.JoinEdgeViewsResult
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * An application service for [GraphView] that enhances the domain services with undo/redo functionality
 * using [Command]s.
 */
interface GraphViewService : DrawingService {
	// TODO More service methods
}

open class GraphViewServiceImpl(
	private val commandManager: CommandManager = EditModule.commandManager,
	private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : DrawingServiceImpl(commandManager), GraphViewService {

	companion object {
		private val LOG by logger(GraphViewServiceImpl::class)
	}

	/** ---- [DrawingService] interface */

	override fun add(component: Component, drawingView: DrawingView<Drawing<Component>>) {
		if (drawingView.drawing !is GraphView<*> || component is GraphElementView<*>) {
			super.add(component, drawingView)
		} else {
			super.add(GraphElementViewWrapper(component), drawingView)
		}
	}

	override fun delete(components: List<Component>, drawingView: DrawingView<Drawing<Component>>, cmdDescriptionKey: String?) {
		LOG.debug("delete ${components.size} components")
		commandManager.beginTransaction(cmdDescriptionKey ?: "edit.command.delete", drawingView)

		for (component in components) {
			if (component is VerticeView<*>) {
				unconnectDeletedVerticeView(component, drawingView.drawing as GraphView<*>)
			} else if (component is EdgeView<*>) {
				unconnectDeletedEdgeView(component as EdgeView<Any>, drawingView.drawing as GraphView<GraphElementView<*>>)
			}
		}

		commandManager.execute(DeleteCommand(drawingView, components.map { possibleWrapper(it, drawingView.drawing) }))
		commandManager.commitTransaction()
	}

	/** ---- [GraphViewServiceImpl] */

	private fun unconnectDeletedVerticeView(verticeView: VerticeView<*>, graphView: GraphView<*>) {
		LOG.debug("unconnectDeletedVerticeView for verticeView ${verticeView.id}")
		graphView.getEdgeViews()
			.filter { ev -> ev.origin?.connectableView === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewOriginCommand(connectService, ev)) }
		graphView.getEdgeViews()
			.filter { ev -> ev.destination?.connectableView === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewDestinationCommand(connectService, ev)) }
	}

	private fun unconnectDeletedEdgeView(edgeView: EdgeView<Any>, graphView: GraphView<GraphElementView<*>>) {
		LOG.debug("unconnectDeletedEdgeView for verticeView ${edgeView.id}")
		commandManager.execute(UnconnectEdgeViewCommand(connectService, graphView, edgeView))
	}

	private fun getWrapperOf(component: Component, drawing: Drawing<Component>): GraphElementViewWrapper? {
		return drawing.getDrawables()
			.filter { it is GraphElementViewWrapper && it.component === component }
			.map { it as GraphElementViewWrapper }
			.firstOrNull()
	}

	private fun possibleWrapper(component: Component, drawing: Drawing<Component>): Component {
		return getWrapperOf(component, drawing) ?: component
	}
}

private class UnconnectEdgeViewCommand(
	private val connectService: GraphViewConnectService,
	private val graphView: GraphView<GraphElementView<*>>,
	private val edgeView: EdgeView<Any>
) : AbstractCommand("graph.command.unconnectEdgeView", null) {

	private val origin = edgeView.origin
	private val destination = edgeView.destination

	private lateinit var joinResults: Pair<JoinEdgeViewsResult<Any>?>

	override fun execute() {
		joinResults = connectService.unconnect(edgeView)
	}

	override fun undo() {
		if (joinResults.first == null) {
			origin?.let { connectService.connectToOrigin(edgeView, it) }
		} else {
			connectService.split(
				graphView,
				joinResults.first!!.joinedEdgeView,
				joinResults.first!!.segmentIndex,
				joinResults.first!!.removedEdgeView,
				joinResults.first!!.removedEdgeViewEndpointType,
				joinResults.first!!.targetPortView,
				joinResults.first!!.tailEdgeView)
		}

		if (joinResults.second == null) {
			destination?. let { connectService.connectToDestination(edgeView, destination) }
		} else {
			connectService.split(
				graphView,
				joinResults.second!!.joinedEdgeView,
				joinResults.second!!.segmentIndex,
				joinResults.second!!.removedEdgeView,
				joinResults.second!!.removedEdgeViewEndpointType,
				joinResults.second!!.targetPortView,
				joinResults.second!!.tailEdgeView)
		}
	}
}

private class UnconnectEdgeViewOriginCommand(
	private val connectService: GraphViewConnectService,
	private val edgeView: EdgeView<Any>
) : AbstractCommand("graph.command.unconnectEdgeViewOrigin", null) {

	private val connection = edgeView.origin!!

	override fun execute() {
		connectService.unconnectEdgeViewOrigin(edgeView)
	}

	override fun undo() {
		connectService.connectToOrigin(edgeView, connection)
	}
}

private class UnconnectEdgeViewDestinationCommand(
	private val connectService: GraphViewConnectService,
	private val edgeView: EdgeView<Any>
) : AbstractCommand("graph.command.unconnectEdgeViewDestination", null) {

	private val connection = edgeView.destination!!

	override fun execute() {
		connectService.unconnectEdgeViewDestination(edgeView)
	}

	override fun undo() {
		connectService.connectToDestination(edgeView, connection)
	}
}
