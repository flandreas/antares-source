package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType

class JoinEdgeViewEndpointsCommand<T : Any>(
	editor: Editor,
	private val connectService: GraphViewConnectService,
	private val movedEdgeViewId: Int,
	private val movedEndpointType: EdgeViewEndpointType,
	private val joinedEdgeViewId: Int,
	private val joinedEndpointType: EdgeViewEndpointType
) : AbstractCommand("graph.command.joinEndpoints", editor) {

	private val graphView: GraphView get() = editor!!.drawing as GraphView
	private val movedEdgeView get() = editor!!.drawing.getWithId(movedEdgeViewId) as EdgeView<T>
	private val joinedEdgeView get() = editor!!.drawing.getWithId(joinedEdgeViewId) as EdgeView<T>

	override fun getDetailedDescription(): String =
		"${super.getDetailedDescription()} type:$movedEndpointType movedId:$movedEdgeViewId joinedId:$joinedEdgeViewId"

	override fun execute() {
		connectService.join(graphView, movedEdgeView, movedEndpointType, joinedEdgeView, joinedEndpointType)
	}
}