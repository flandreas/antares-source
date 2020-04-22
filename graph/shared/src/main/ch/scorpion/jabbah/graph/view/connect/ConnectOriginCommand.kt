package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * Connects the origin point of an [EdgeView] with the [Port] of a [VerticeView].
 */
class ConnectOriginCommand(
	editor: Editor,
	private val service: GraphViewConnectService,
	private val edgeViewId: Int,
	private val origConnectableViewId: Int,
	private val origPortId: Int
) : AbstractCommand("graph.command.connect", editor) {

	private val edgeView: EdgeView<*> get() = editor!!.drawing.getWithId(edgeViewId) as EdgeView<*>
	private val origConnectableView get() = editor!!.drawing.getWithId(origConnectableViewId) as ConnectableView
	private val origPort: Port<*> get() = origConnectableView.getPort(origPortId)!!

	override fun execute() {
		service.connectToOrigin(edgeView as EdgeView<Any>, Connection(origConnectableView, origPort as Port<Any>))
	}

	override fun undo() {
		// TODO Remove
		//service.unconnectFromOrigin(edgeView);
	}
}