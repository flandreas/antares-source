package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.DeleteCommand
import ch.scorpion.jabbah.edit.app.DrawingService
import ch.scorpion.jabbah.edit.app.DrawingServiceImpl
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.connect.JoinEdgeViewsResult
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * An application service for [GraphView] that enhances the domain services with undo/redo functionality
 * using [Command]s.
 */
interface GraphViewService : DrawingService {
	// TODO More service methods
}

class GraphViewServiceImpl(
	private val commandManager: CommandManager = EditModule.commandManager,
	private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : DrawingServiceImpl(commandManager), GraphViewService {

	companion object {
		private val LOG by logger(GraphViewServiceImpl::class)
	}

	/** ---- [DrawingService] interface */

	override fun add(component: Component, drawingView: DrawingView<Drawing<Component>>) {
		if (component is GraphElementView<*>) {
			super.add(component, drawingView)
		}
		super.add(GraphElementViewWrapper<GraphElement>(component), drawingView)
	}

	override fun delete(components: List<Component>, drawingView: DrawingView<Drawing<Component>>) {
		LOG.debug("delete ${components.size} components")
		commandManager.beginTransaction("edit.command.delete", drawingView)

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
			.filter { ev -> ev.origin === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewOriginCommand(connectService, ev)) }
		graphView.getEdgeViews()
			.filter { ev -> ev.destination === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewDestinationCommand(connectService, ev)) }
	}

	private fun unconnectDeletedEdgeView(edgeView: EdgeView<Any>, graphView: GraphView<GraphElementView<*>>) {
		LOG.debug("unconnectDeletedEdgeView for verticeView ${edgeView.id}")
		commandManager.execute(UnconnectEdgeViewCommand(connectService, graphView, edgeView))
	}

	private fun getWrapperOf(component: Component, drawing: Drawing<Component>): GraphElementViewWrapper<*>? {
		return drawing.getDrawables()
			.filter { it is GraphElementViewWrapper<*> && it.component === component }
			.map { it as GraphElementViewWrapper<*> }
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

	private val origPortView: PortView<Any>? =
		if (edgeView.originPort != null) edgeView.origin!!.getPortView(edgeView.originPort!!) else null
	private val destPortView: PortView<Any>? =
		if (edgeView.destinationPort != null) edgeView.destination!!.getPortView(edgeView.destinationPort!!) else null

	private var joinResults: JoinEdgeViewsResult<Any>? = null

	override fun execute() {
		joinResults = connectService.unconnect(edgeView)
	}

	override fun undo() {
		if (joinResults == null) {
			connectService.connect(edgeView, origPortView, destPortView)
		} else {
			if (joinResults != null) {
				connectService.split(
					graphView,
					joinResults!!.joinedEdgeView,
					joinResults!!.segmentIndex,
					joinResults!!.removedEdgeView,
					joinResults!!.targetPortView,
					joinResults!!.tailEdgeView)
			}
		}
	}
}

private class UnconnectEdgeViewOriginCommand(
	private val connectService: GraphViewConnectService,
	private val edgeView: EdgeView<Any>
) : AbstractCommand("graph.command.unconnectEdgeViewOrigin", null) {

	private val connectableView = edgeView.origin
	private val port = edgeView.originPort

	override fun execute() {
		connectService.unconnectEdgeViewOrigin(edgeView)
	}

	override fun undo() {
		connectService.connectToOrigin(edgeView, connectableView!!, port)
	}
}

private class UnconnectEdgeViewDestinationCommand(
	private val connectService: GraphViewConnectService,
	private val edgeView: EdgeView<Any>
) : AbstractCommand("graph.command.unconnectEdgeViewDestination", null) {

	private val connectableView = edgeView.destination
	private val port = edgeView.destinationPort

	override fun execute() {
		connectService.unconnectEdgeViewDestination(edgeView)
	}

	override fun undo() {
		connectService.connectToDestination(edgeView, connectableView!!, port)
	}
}
