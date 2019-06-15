package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.net.node.NodeView

/**
 * A [Command] that splits an [EdgeView] by inserting a [NodeView], to which an additional
 * [EdgeView] is connected (which is open-ended).
 */
class SplitEdgeViewCommand(
	editor: Editor,
	private val connectService: GraphViewConnectService,
	private val graphView: GraphView<GraphElementView<*>>,
	private var origEdgeView: EdgeView<*>,
	private val segmentIndex: Int,
	private val newEdgeView: EdgeView<*>,
	private val targetPortView: PortView<*>?,
	private val nodeView: NodeView<*>?
) : AbstractCommand("graph.command.splitEdge", editor) {

	private var result: SplitEdgeViewResult<Any>? = null

	override fun execute() {
		result = connectService.split(graphView, origEdgeView as EdgeView<Any>, segmentIndex, newEdgeView as EdgeView<Any>, targetPortView as PortView<Any>?);
	}

	override fun undo() {
		origEdgeView = connectService.unconnect(result!!.newEdgeView)!!.joinedEdgeView
		graphView.remove(result!!.newEdgeView)
	}

	override fun registered() {
		result = SplitEdgeViewResult(newEdgeView = newEdgeView as EdgeView<Any>, nodeView = nodeView as NodeView<Any>, tailEdgeView = newEdgeView)
	}
}