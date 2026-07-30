package io.antarescircuit.jabbah.graph.view.editor

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.connect.ConnectDestinationCommand
import io.antarescircuit.jabbah.graph.view.connect.ConnectOriginCommand
import io.antarescircuit.jabbah.graph.view.connect.GraphViewConnectService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType.DESTINATION
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType.ORIGIN
import io.antarescircuit.jabbah.graph.view.port.PortView

/**
 * Tries to automatically connect [PortView]s of a [VerticeView] being dragged by the user
 * to [PortView]s of other [VerticeView]s, or to open-ended [EdgeView]s.
 *
 * During dragging, it highlights points where connections can be established by displaying
 * an [AutoConnectorHighlight] in the current [GraphView]. When dragging has finished,
 * it creates the necessary [Command]s to create the matching connections and returns them from
 * [handleDragFinished].
 */
object AutoConnector : DragManagerPlugin {

	/** Used for highlighting the current possible connection points.*/
	private val highlight = AutoConnectorHighlight()

	private val connectService: GraphViewConnectService by lazy { GraphViewModule.graphViewConnectService }

	/** Determines whether [highlight] is currently displayed or not.*/
	private var isHighlightDisplayed = false

	/** Contains the [Point2D]s where a connection is currently possible.*/
	private val points = mutableListOf<Point2D>()

	/** Contains the [Point2D]s where a connection is currently not possible, i.e. denied.*/
	private val denyPoints = mutableListOf<Point2D>()

	/**
	 * Contains the [Port]s of the dragged [VerticeView] that have already matched been matched
	 * to prevent matching them more than once.
	 */
	private val ports = mutableListOf<Port<*>>()

	private val commands = mutableListOf<Command>()

	private var lastMatchLocation: Point2D? = null

	private lateinit var editor: Editor

	private enum class Mode {
		Points,
		Commands
	}

	private var mode: Mode = Mode.Points

	override fun handleDragged(editor: Editor, component: Component) {
		if (component is VerticeView<*>) {
			handleDragged(editor, component)
		}
	}

	override fun handleDragFinished(editor: Editor, component: Component): Collection<Command> {
		this.editor = editor
		lastMatchLocation = null
		return if (component is VerticeView<*>) {
			createCommands(editor.drawing as GraphView, component)
		} else {
			emptySet()
		}
	}

	override fun handleDragTerminated(editor: Editor) {
		removeHighlight(editor.view)
		lastMatchLocation = null
	}

	private fun handleDragged(editor: Editor, draggedVerticeView: VerticeView<*>) {
		if (lastMatchLocation != null && draggedVerticeView.location == lastMatchLocation) {
			return
		}

		matchPoints(editor.drawing as GraphView, draggedVerticeView)

		if (points.isNotEmpty() || denyPoints.isNotEmpty()) {
			if (!isHighlightDisplayed) {
				highlight.setPoints(points, denyPoints)
				addHighlight(editor.view)
			}
		} else {
			if (isHighlightDisplayed) {
				removeHighlight(editor.view)
			}
		}
	}

	/**
	 * Fills the connection point locations of all [PortView]s of the current [VerticeView] that match
	 * an [EdgeView] endpoint into [points].
	 */
	private fun matchPoints(graphView: GraphView, draggedVerticeView: VerticeView<*>) {
		mode = Mode.Points
		points.clear()
		denyPoints.clear()
		ports.clear()
		graphView.getDrawableIntersection(draggedVerticeView).forEach {
			matchOtherDrawable(draggedVerticeView, it, graphView)
		}
		lastMatchLocation = draggedVerticeView.location
	}

	private fun createCommands(graphView: GraphView, verticeView: VerticeView<*>): Collection<Command> {
		mode = Mode.Commands
		commands.clear()
		ports.clear()
		graphView.getDrawableIntersection(verticeView).forEach {
			matchOtherDrawable(verticeView, it, graphView)
		}
		return commands
	}

	private fun matchOtherDrawable(draggedVerticeView: VerticeView<*>, drawable: Drawable, graphView: GraphView) {
		when (drawable) {
			is VerticeView<*> -> {
				draggedVerticeView.getPortViews()
					.filter { !it.port.isConnected }
					.forEach { matchPortViewOfOtherVerticeView(draggedVerticeView, it, drawable) }
			}
			is EdgeView<*> -> {
				draggedVerticeView.getPortViews()
					.filter { !it.port.isConnected }
					.forEach { matchOpenEndpointOfOtherEdgeView(draggedVerticeView, it, drawable, graphView) }
			}
			else -> {}
		}
	}

	private fun matchOpenEndpointOfOtherEdgeView(draggedVerticeView: VerticeView<*>, portView: PortView<*>, ev: EdgeView<*>, graphView: GraphView) {
		if (ports.contains(portView.port)) {
			return
		}
		val p = portView.owner!!.getPortConnectionPoint(portView.port)
		if (ev.origin == null && p == ev.originEndpointView.location) {
			ev.net?.let { net ->
				if (!ORIGIN.canConnectTo(portView.port, net, graphView)) {
					if (mode == Mode.Points) {
						denyPoints.add(p)
					}
					return
				}
			}
			ports.add(portView.port)
			when (mode) {
				Mode.Points -> points.add(p)
				Mode.Commands -> {
					commands.add(ConnectOriginCommand(editor, connectService, ev.id, draggedVerticeView.id, portView.port.portId))
					GraphViewModule.connectionEstablishedHandler?.handle(editor, portView.port)?.let { cmd ->
						commands.add(cmd)
					}
				}
			}
		}
		if (ev.destination == null && p == ev.destinationEndpointView.location) {
			ev.net?.let { net ->
				if (!DESTINATION.canConnectTo(portView.port, net, graphView)) {
					if (mode == Mode.Points) {
						denyPoints.add(p)
					}
					return
				}
			}
			ports.add(portView.port)
			when (mode) {
				Mode.Points -> points.add(p)
				Mode.Commands -> {
					commands.add(ConnectDestinationCommand(editor, connectService, ev.id, draggedVerticeView.id, portView.port.portId))
					GraphViewModule.connectionEstablishedHandler?.handle(editor, portView.port)?.let { cmd ->
						commands.add(cmd)
					}
				}
			}
		}
	}

	private fun matchPortViewOfOtherVerticeView(draggedVerticeView: VerticeView<*>, portView: PortView<*>, otherVerticeView: VerticeView<*>) {
		otherVerticeView.getPortViews()
			.filter { !it.port.isConnected }
			.forEach {
				if (!ports.contains(portView.port) && portView.owner!!.getPortConnectionPoint(portView.port) == it.owner!!.getPortConnectionPoint(it.port)) {
					if (ORIGIN.canConnectTo(portView.port) && DESTINATION.canConnectTo(it.port)
						|| DESTINATION.canConnectTo(portView.port) && ORIGIN.canConnectTo(it.port)
					) {
						ports.add(portView.port)
						when (mode) {
							Mode.Points ->
								points.add(portView.owner!!.getPortConnectionPoint(portView.port))
							Mode.Commands -> {
								commands.add(
									AutoConnectCommand(
										editor,
										draggedVerticeView.id,
										portView.port.portId,
										otherVerticeView.id,
										it.port.portId)
								)
								GraphViewModule.connectionEstablishedHandler?.handle(editor, portView.port, it.port)?.let { cmd ->
									commands.add(cmd)
								}
							}
						}
					} else {
						ports.add(portView.port)
						if (mode == Mode.Points) {
							denyPoints.add(portView.owner!!.getPortConnectionPoint(portView.port))
						}
					}
				}
			}
	}

	private fun addHighlight(drawingView: DrawingView<*,*>) {
		drawingView.animationContainer.add(highlight)
		highlight.validate()
		isHighlightDisplayed = true
	}

	private fun removeHighlight(drawingView: DrawingView<*,*>) {
		if (drawingView.animationContainer.contains(highlight)) {
			drawingView.animationContainer.remove(highlight)
			drawingView.drawing.validate()
		}
		isHighlightDisplayed = false
	}
}
