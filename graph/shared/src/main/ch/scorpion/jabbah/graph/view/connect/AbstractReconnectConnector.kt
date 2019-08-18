package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * A base class for building connectors that reconnect [EdgeView]s to other [PortView]s, or turn
 * the formerly connected [EdgeView] into being unconnected.
 */
abstract class AbstractReconnectConnector(
	draggedEndpointType: EdgeViewEndpointType,
	protected val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	protected val eventBus: EventBus = BaseModule.eventBus
) : AbstractDragEdgeViewEndpointConnector(draggedEndpointType) {

	companion object {
		const val MIN_DRAG_DISTANCE = 10
	}

	/**
	 * The location where the mouse was pressed. Used to rollback the unconnect action if the user
	 * didn't drag the mouse far enough, assuming that he clicked accidentally.
	 */
	protected var pressLocation: Point2D = Point2D.ZERO

}