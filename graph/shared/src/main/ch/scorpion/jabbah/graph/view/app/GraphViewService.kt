package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.DeleteCommand
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.edit.app.DrawingService
import ch.scorpion.jabbah.edit.app.DrawingServiceImpl
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * An application service for [GraphView] that enhances the domain services with undo/redo functionality
 * using [Command]s.
 */
interface GraphViewService : DrawingService {
    // TODO More service methods
}

class GraphViewServiceImpl(
        private val commandManager: CommandManager = EditModule.commandManager,
        private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
        private val eventBus: EventBus = BaseModule.eventBus
) : DrawingServiceImpl(commandManager), GraphViewService {

    /** ---- [DrawingServiceImpl] */

    override fun delete(components: List<Component>, drawingView: DrawingView<Drawing<Component>>) {
        commandManager.beginTransaction("edit.command.delete", drawingView)

        for (component in components) {
            if (component is VerticeView<*>) {
                (drawingView.drawing as GraphView<*>).getEdgeViews()
                        .filter { ev -> ev.origin === component }
                        .forEach { ev -> commandManager.execute(UnconnectEdgeViewOriginCommand(connectService, ev)) }
                (drawingView.drawing as GraphView<*>).getEdgeViews()
                        .filter { ev -> ev.destination === component }
                        .forEach { ev -> commandManager.execute(UnconnectEdgeViewDestinationCommand(connectService, ev)) }
            }
        }

        commandManager.execute(DeleteCommand(drawingView, components))
        commandManager.commitTransaction()
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
