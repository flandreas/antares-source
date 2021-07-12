package ch.scorpion.jabbah.graph.view.editor

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.connect.ConnectDestinationCommand
import ch.scorpion.jabbah.graph.view.connect.ConnectOriginCommand
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

object AutoConnector {

	/** Used for highlighting the current possible connection points.*/
	private val highlight = AutoConnectorHighlight()

	/** Determines whether [highlight] is currently displayed or not.*/
	private var isHighlightDisplayed = false

	/** Contains the [Point2D]s where a connection is currently possible.*/
	private val points = mutableListOf<Point2D>()

	fun handleDragged(editor: Editor, verticeView: VerticeView<*>) {
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

	fun handleDragFinished(editor: Editor) {
		removeHighlight(editor.view)
	}

	fun createAutoConnectCommands(
		editor: Editor,
		verticeView: VerticeView<*>,
		service: GraphViewConnectService = GraphViewModule.graphViewConnectService
	): Collection<Command> {
		val commands = mutableListOf<Command>()

		(verticeView.parent as GraphView).getEdgeViews()
			.filter { it.origin == null || it.destination == null }
			.forEach { ev ->
				verticeView.getPortViews().forEach {
					val p = it.owner!!.getPortConnectionPoint(it.port)
					if (ev.origin == null && it.port.portType.isOutput && p == ev.originEndpointView.location) {
						commands.add(ConnectOriginCommand(editor, service, ev.id, verticeView.id, it.port.portId))
					}
					if (ev.destination == null && it.port.portType.isInput && p == ev.destinationEndpointView.location) {
						commands.add(ConnectDestinationCommand(editor, service, ev.id, verticeView.id, it.port.portId))
					}
				}
			}

		return commands
	}

	/**
	 * Fills the connection point locations of all [PortView]s of the current [VerticeView] that match
	 * an [EdgeView] endpoint into [points].
	 */
	private fun matchPoints(graphView: GraphView, verticeView: VerticeView<*>) {
		points.clear()
		graphView.getEdgeViews()
			.filter { it.origin == null || it.destination == null }
			.forEach { ev ->
				verticeView.getPortViews().forEach {
					val p = it.owner!!.getPortConnectionPoint(it.port)
					if (ev.origin == null && it.port.portType.isOutput && p == ev.originEndpointView.location) {
						points.add(p)
					}
					if (ev.destination == null && it.port.portType.isInput && p == ev.destinationEndpointView.location) {
						points.add(p)
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
		drawingView.animationContainer.remove(highlight)
		drawingView.drawing.validate()
		isHighlightDisplayed = false
	}
}
