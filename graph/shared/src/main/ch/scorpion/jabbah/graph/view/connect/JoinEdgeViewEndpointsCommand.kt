package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType

class JoinEdgeViewEndpointsCommand<T : Any>(
	editor: Editor,
	private val connectService: GraphViewConnectService,
	private val movedEdgeViewId: Int,
	private val movedEndpointType: EdgeViewEndpointType,
	private val location: Point2D,
	private val joinedEdgeViewId: Int,
) : AbstractCommand("graph.command.joinEndpoints", editor) {

	private val graphView: GraphView get() = editor!!.drawing as GraphView
	private val movedEdgeView get() = editor!!.drawing.getWithId(movedEdgeViewId) as EdgeView<T>
	private val joinedEdgeView get() = editor!!.drawing.getWithId(joinedEdgeViewId) as EdgeView<T>

	override fun execute() {
		connectService.join(graphView, movedEdgeView, movedEndpointType, location, joinedEdgeView)
	}
}