package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.collection.Pair
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.DeleteCommand
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.app.DrawingAppServiceImpl
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
interface GraphViewAppService : DrawingAppService {
	// TODO More service methods
}

open class GraphViewAppServiceImpl(
	private val commandManager: CommandManager = EditModule.commandManager,
	private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : DrawingAppServiceImpl(commandManager), GraphViewAppService {

	companion object {
		private val LOG by logger(GraphViewAppServiceImpl::class)
	}

	/** ---- [DrawingAppService] interface */

	override fun add(component: Component, drawingView: DrawingView<Drawing<Component>>) {
		if (drawingView.drawing !is GraphView || component is GraphElementView<*>) {
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
				unconnectDeletedVerticeView(component, drawingView as DrawingView<GraphView>)
			} else if (component is EdgeView<*>) {
				if (drawingView.drawing.contains(component)) {
					// Might have been joined away by a previous removal of another EdgeView
					unconnectDeletedEdgeView(component as EdgeView<Any>, drawingView as DrawingView<GraphView>)
				}
			}
		}

		commandManager.execute(DeleteCommand(
			drawingView,
			components
				.filter { drawingView.drawing.contains(it) }
				.map { possibleWrapper(it, drawingView.drawing).id }))

		commandManager.commitTransaction()
	}

	/** ---- [GraphViewAppServiceImpl] */

	private fun unconnectDeletedVerticeView(verticeView: VerticeView<*>, drawingView: DrawingView<GraphView>) {
		LOG.debug("unconnectDeletedVerticeView for verticeView ${verticeView.id}")
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.origin?.connectableView === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewOriginCommand(drawingView, connectService, ev.id)) }
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.destination?.connectableView === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewDestinationCommand(drawingView, connectService, ev.id)) }
	}

	private fun unconnectDeletedEdgeView(edgeView: EdgeView<Any>, drawingView: DrawingView<GraphView>) {
		LOG.debug("unconnectDeletedEdgeView for verticeView ${edgeView.id}")
		commandManager.execute(UnconnectEdgeViewCommand(drawingView, connectService, edgeView.id))
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
	private val drawingView: DrawingView<GraphView>,
	private val connectService: GraphViewConnectService,
	private val edgeViewId: Int
) : AbstractCommand("graph.command.unconnectEdgeView", null) {

	private val edgeView get() = drawingView.drawing.getWithId(edgeViewId) as EdgeView<Any>

	lateinit var joinResults: Pair<JoinEdgeViewsResult<Any>?>
		private set

	override fun execute() {
		joinResults = connectService.unconnect(edgeView)
	}

	override fun undo() {
		// TODO Remove
		/*
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
			destination?.let { connectService.connectToDestination(edgeView, destination) }
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
		*/
	}
}

private class UnconnectEdgeViewOriginCommand(
	private val drawingView: DrawingView<GraphView>,
	private val connectService: GraphViewConnectService,
	private val edgeViewId: Int
) : AbstractCommand("graph.command.unconnectEdgeViewOrigin", null) {

	private val edgeView get() = drawingView.drawing.getWithId(edgeViewId) as EdgeView<Any>

	override fun execute() {
		connectService.unconnectEdgeViewOrigin(edgeView)
	}

	override fun undo() {
		// TODO Remove
		//connectService.connectToOrigin(edgeView, connection)
	}
}

private class UnconnectEdgeViewDestinationCommand(
	private val drawingView: DrawingView<GraphView>,
	private val connectService: GraphViewConnectService,
	private val edgeViewId: Int
) : AbstractCommand("graph.command.unconnectEdgeViewDestination", null) {

	private val edgeView get() = drawingView.drawing.getWithId(edgeViewId) as EdgeView<Any>

	override fun execute() {
		connectService.unconnectEdgeViewDestination(edgeView)
	}

	override fun undo() {
		// TODO Remove
		//connectService.connectToDestination(edgeView, connection)
	}
}
