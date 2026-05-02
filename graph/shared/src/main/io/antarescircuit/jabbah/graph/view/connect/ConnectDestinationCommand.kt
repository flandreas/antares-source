package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.model.OutputPort
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.ConnectableView
import io.antarescircuit.jabbah.graph.view.Connection
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.VerticeView

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

	override fun getDetailedDescription(): String =
		"${super.getDetailedDescription()} $edgeViewId dest:$destConnectableViewId:$destPortId"

	@Suppress("UNCHECKED_CAST")
	override fun execute() {
		service.connectToDestination(edgeView as EdgeView<Any>, Connection(destConnectableView, destPort as Port<Any>))
	}
}