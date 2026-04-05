package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.ConnectableView
import io.antarescircuit.jabbah.graph.view.Connection
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.VerticeView

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

	override fun getDetailedDescription(): String =
		"${super.getDetailedDescription()} $edgeViewId orig:$origConnectableViewId:$origPortId"

	override fun execute() {
		service.connectToOrigin(edgeView as EdgeView<Any>, Connection(origConnectableView, origPort as Port<Any>))
	}
}