package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.io.StorableCloner

/**
 * A [Command] that splits an [EdgeView] by inserting a [NodeView], to which an additional
 * [EdgeView] is connected (which is open-ended).
 *
 * Creates a clone of [newEdgeView] and adds that one to the [GraphView], which is necessary to add the original
 * value when being re-executed after undo. Retrieve the effectively added [EdgeView] in [addedNewEdgeView].
 */
class SplitEdgeViewCommand(
	editor: Editor,
	private val connectService: GraphViewConnectService,
	private var splitEdgeViewId: Int,
	private val segmentIndex: Int,
	private val newEdgeView: EdgeView<*>,
	private val newEdgeViewEndpointType: EdgeViewEndpointType,
	private val targetConnectableViewId: Int?,
	private val targetPortId: Int?
) : AbstractCommand("graph.command.splitEdge", editor) {

	companion object {
		private val LOG by logger(SplitEdgeViewCommand::class)
	}

	private val graphView: GraphView get() = editor!!.drawing as GraphView
	private val splitEdgeView: EdgeView<Any> get() = editor!!.drawing.getWithId(splitEdgeViewId) as EdgeView<Any>
	private val targetConnectableView: ConnectableView? get() = targetConnectableViewId?.let { editor!!.drawing.getWithId(it) as ConnectableView }
	private val targetPortView: PortView<*>? get() = targetPortId?.let { targetConnectableView!!.getPortView(targetConnectableView!!.getPort(it)!!) }

	lateinit var result: SplitEdgeViewResult<Any>
	lateinit var addedNewEdgeView: EdgeView<Any>

	override fun execute() {
		LOG.debug("Execute on GraphView ${graphView.hashCode().toString(16)}")

		addedNewEdgeView = StorableCloner.clone(newEdgeView) as EdgeView<Any>

		result = connectService.split(
			graphView,
			splitEdgeView,
			segmentIndex,
			addedNewEdgeView,
			newEdgeViewEndpointType,
			targetPortView as PortView<Any>?)
	}
}