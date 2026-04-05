package io.antarescircuit.jabbah.graph.view.editor

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.connect.GraphViewConnectService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.port.PortView

/**
 * Creates and inserts a new [EdgeView] to connect a origin [VerticeView]'s [PortView]
 * with the [PortView] of a destination [VerticeView].
 */
class AutoConnectCommand(
	editor: Editor,
	private val originVerticeViewId: Int,
	private val originPortId: Int,
	private val destinationVerticeViewId: Int,
	private val destinationPortId: Int,
	private val service: GraphViewConnectService = GraphViewModule.graphViewConnectService,
) : AbstractCommand("graph.command.autoConnect", editor) {

	private val originVerticeView: VerticeView<*> get() = editor!!.drawing.getWithId(originVerticeViewId) as VerticeView<*>
	private val destinationVerticeView: VerticeView<*> get() = editor!!.drawing.getWithId(destinationVerticeViewId) as VerticeView<*>

	override fun getDetailedDescription(): String =
		"${super.getDetailedDescription()} origin=$originVerticeViewId:$originPortId destination=$destinationVerticeViewId:$destinationPortId"

	override fun execute() {
		service.addConnection(
			editor!!.drawing as GraphView,
			originVerticeView.getPortView(originVerticeView.model.getPort(originPortId)) as PortView<Any>,
			destinationVerticeView.getPortView(destinationVerticeView.model.getPort(destinationPortId)) as PortView<Any>)
	}
}
