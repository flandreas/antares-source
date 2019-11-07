package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * Connects the destination point of an [EdgeView] with the [OutputPort] of a [VerticeView].
 */
class ConnectDestinationCommand(
	editor: Editor,
	private val service: GraphViewConnectService,
	private val edgeView: EdgeView<*>,
	private val destConnectableView: ConnectableView,
	private val destPort: Port<*>
) : AbstractCommand("graph.command.connect", editor) {

	override fun execute() {
		service.connectToDestination(edgeView as EdgeView<Any>, Connection(destConnectableView, destPort as Port<Any>))
	}

	override fun undo() {
		service.unconnectFromDestination(edgeView);
	}
}