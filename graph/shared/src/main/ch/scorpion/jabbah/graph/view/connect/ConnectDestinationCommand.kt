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
	private val edgeViewId: Int,
	private val destConnectableViewId: Int,
	private val destPortId: Int
) : AbstractCommand("graph.command.connect", editor) {

	private val edgeView: EdgeView<*> get() = editor!!.drawing.getWithId(edgeViewId) as EdgeView<*>
	private val destConnectableView get() = editor!!.drawing.getWithId(destConnectableViewId) as ConnectableView
	private val destPort: Port<*> get() = destConnectableView.getPort(destPortId)!!

	override fun execute() {
		service.connectToDestination(edgeView as EdgeView<Any>, Connection(destConnectableView, destPort as Port<Any>))
	}
}