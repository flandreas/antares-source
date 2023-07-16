package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
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

interface NewEdgeViewAtSplitProvider {
	fun provide(): EdgeView<*>
}

/**
 * Creates a clone of [newEdgeView] and adds that one to the [GraphView], which is necessary to add the original
 * value when being re-executed after undo.
 */
class NewEdgeViewAtSplitCloneProvider(
	private val newEdgeView: EdgeView<*>
) : NewEdgeViewAtSplitProvider {
	override fun provide(): EdgeView<*> = StorableCloner.clone(newEdgeView)
}

class NewEdgeViewAtSplitRetrieveProvider(
	private val editor: Editor,
	private val newEdgeViewId: Int
) : NewEdgeViewAtSplitProvider {
	override fun provide(): EdgeView<*> = editor.drawing.getWithId(newEdgeViewId) as EdgeView<*>
}

/**
 * A [Command] that splits an [EdgeView] by inserting a [NodeView], to which an additional
 * [EdgeView] is connected (which is open-ended).
 *
 * Retrieve the effectively added [EdgeView] in [addedNewEdgeView].
 */
class SplitEdgeViewCommand(
	editor: Editor,
	baseKey: String = "graph.command.splitEdge",
	private val connectService: GraphViewConnectService,
	private val splitEdgeViewId: Int,
	private val segmentIndex: Int,
	private val splitLocation: Point2D,
	private val newEdgeViewProvider: NewEdgeViewAtSplitProvider,
	private val newEdgeViewEndpointType: EdgeViewEndpointType,
	private val targetConnectableViewId: Int?,
	private val targetPortId: Int?
) : AbstractCommand(baseKey, editor) {

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
		LOG.trace("Execute on GraphView ${graphView.hashCode().toString(16)}")

		addedNewEdgeView = newEdgeViewProvider.provide() as EdgeView<Any>

		result = connectService.split(
			graphView,
			splitEdgeView,
			segmentIndex,
			splitLocation,
			addedNewEdgeView,
			newEdgeViewEndpointType,
			targetPortView as PortView<Any>?)
	}
}