package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType
import io.antarescircuit.jabbah.graph.view.port.PortView

/**
 * A base class for building connectors that reconnect [EdgeView]s to other [PortView]s, or turn
 * the formerly connected [EdgeView] into being unconnected.
 */
abstract class AbstractReconnectConnector(
	draggedEndpointType: EdgeViewEndpointType,
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	protected val eventBus: EventBus = BaseModule.eventBus
) : AbstractDragEdgeViewEndpointConnector(connectService, draggedEndpointType) {

	companion object {
		const val MIN_DRAG_DISTANCE = 10
	}

	/**
	 * The location where the mouse was pressed. Used to rollback the unconnect action if the user
	 * didn't drag the mouse far enough, assuming that he clicked accidentally.
	 */
	protected var pressLocation: Point2D = Point2D.ZERO

	override fun displayPortViewHighlight(context: EditInputEventContext) {
		ConnectionPointHighlighter.displayPortViewHighlight(
			context.drawingView,
			getEndpointView().location,
			highlight = DrawModule.properties.get(PortView.PROP_HIGHLIGHT_RECONNECT)
		)
	}
}