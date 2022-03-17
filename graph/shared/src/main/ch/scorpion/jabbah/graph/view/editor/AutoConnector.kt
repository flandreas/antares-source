package ch.scorpion.jabbah.graph.view.editor

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.ConnectDestinationCommand
import ch.scorpion.jabbah.graph.view.connect.ConnectOriginCommand
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType.DESTINATION
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType.ORIGIN
import ch.scorpion.jabbah.graph.view.port.PortView

object AutoConnector : DragManagerPlugin {

	/** Used for highlighting the current possible connection points.*/
	private val highlight = AutoConnectorHighlight()

	private val connectService: GraphViewConnectService by lazy { GraphViewModule.graphViewConnectService }

	/** Determines whether [highlight] is currently displayed or not.*/
	private var isHighlightDisplayed = false

	/** Contains the [Point2D]s where a connection is currently possible.*/
	private val points = mutableListOf<Point2D>()

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

	private fun handleDragged(editor: Editor, verticeView: VerticeView<*>) {
		if (lastMatchLocation != null && verticeView.location == lastMatchLocation) {
			return
		}

		matchPoints(editor.drawing as GraphView, verticeView)

		if (points.size > 0) {
			if (!isHighlightDisplayed) {
				highlight.setPoints(points)
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
	private fun matchPoints(graphView: GraphView, verticeView: VerticeView<*>) {
		mode = Mode.Points
		points.clear()
		graphView.getDrawableIntersection(verticeView).forEach {
			matchOtherDrawable(verticeView, it)
		}
		lastMatchLocation = verticeView.location
	}

	private fun createCommands(graphView: GraphView, verticeView: VerticeView<*>): Collection<Command> {
		mode = Mode.Commands
		commands.clear()
		graphView.getDrawableIntersection(verticeView).forEach {
			matchOtherDrawable(verticeView, it)
		}
		return commands
	}

	private fun matchOtherDrawable(verticeView: VerticeView<*>, drawable: Drawable) {
		when (drawable) {
			is VerticeView<*> -> {
				verticeView.getPortViews()
					.filter { !it.port.isConnected }
					.forEach { matchPortViewOfOtherVerticeView(verticeView, it, drawable) }
			}
			is EdgeView<*> -> {
				verticeView.getPortViews()
					.filter { !it.port.isConnected }
					.forEach { matchOpenEndpointOfOtherEdgeView(verticeView, it, drawable) }
			}
			else -> {}
		}
	}

	private fun matchOpenEndpointOfOtherEdgeView(verticeView: VerticeView<*>, portView: PortView<*>, ev: EdgeView<*>) {
		val p = portView.owner!!.getPortConnectionPoint(portView.port)
		if (ev.origin == null && p == ev.originEndpointView.location) {
			ev.net?.let { net ->
				if (!ORIGIN.canConnectTo(portView.port, net)) {
					return
				}
			}
			when (mode) {
				Mode.Points -> points.add(p)
				Mode.Commands -> commands.add(
					ConnectOriginCommand(editor, connectService, ev.id, verticeView.id, portView.port.portId))
			}
		}
		if (ev.destination == null && p == ev.destinationEndpointView.location) {
			ev.net?.let { net ->
				if (!DESTINATION.canConnectTo(portView.port, net)) {
					return
				}
			}
			when (mode) {
				Mode.Points -> points.add(p)
				Mode.Commands -> commands.add(
					ConnectDestinationCommand(editor, connectService, ev.id, verticeView.id, portView.port.portId))
			}
		}
	}

	private fun matchPortViewOfOtherVerticeView(verticeView: VerticeView<*>, portView: PortView<*>, otherVerticeView: VerticeView<*>) {
		otherVerticeView.getPortViews()
			.filter { !it.port.isConnected }
			.forEach {
				if (portView.owner!!.getPortConnectionPoint(portView.port) == it.owner!!.getPortConnectionPoint(it.port)) {
					if (ORIGIN.canConnectTo(portView.port, null) && DESTINATION.canConnectTo(it.port, null)
						|| DESTINATION.canConnectTo(portView.port, null) && ORIGIN.canConnectTo(it.port, null)
					) {
						when (mode) {
							Mode.Points -> points.add(portView.owner!!.getPortConnectionPoint(portView.port))
							Mode.Commands -> commands.add(
								AutoConnectCommand(editor, verticeView.id, portView.port.portId, otherVerticeView.id, it.port.portId))
						}
					}
				}
			}
	}

	private fun addHighlight(drawingView: DrawingView<*>) {
		drawingView.animationContainer.add(highlight)
		highlight.validate()
		isHighlightDisplayed = true
	}

	private fun removeHighlight(drawingView: DrawingView<*>) {
		if (drawingView.animationContainer.contains(highlight)) {
			drawingView.animationContainer.remove(highlight)
			drawingView.drawing.validate()
		}
		isHighlightDisplayed = false
	}
}
