package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Connects the origin point of an [EdgeView] with the [OutputPort] of a [VerticeView].
 */
class ConnectOriginCommand(
        editor: Editor,
        private val service: GraphViewConnectService,
        private val edgeView: EdgeView<*>,
        private val origConnectableView: ConnectableView,
        private val origPort: Port<*>
) : AbstractCommand("graph.command.connect", editor){

    override fun execute() {
        service.connectToOrigin(edgeView as EdgeView<Any>, origConnectableView, origPort as Port<Any>);
    }

    override fun undo() {
        service.unconnectFromOrigin(edgeView);
    }
}