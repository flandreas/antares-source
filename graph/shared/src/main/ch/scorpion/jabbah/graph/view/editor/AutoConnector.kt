package ch.scorpion.jabbah.graph.view.editor

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.DropEvent
import ch.scorpion.jabbah.edit.select.DragEvent
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Listens for [DragEvent]s that represent [Component] drags by a particular [GraphEditor],
 * and tries to auto-connect [VerticeView]s with open-ended [EdgeView]s.
 *
 * Does only respond if a single [VerticeView] is being dragged.
 */
class AutoConnector(
	private val editor: Editor,
	private val connectService: GraphViewConnectService,
	eventBus: EventBus
) {

	constructor(editor: Editor) : this(editor, GraphViewModule.graphViewConnectService, BaseModule.eventBus)

	/** Used for highlighting the current possible connection points.*/
	private val highlight = AutoConnectorHighlight()

	/** Determines whether [highlight] is currently displayed or not.*/
	private var isHighlightDisplayed = false

	/** The [VerticeView] that is currently being dragged around, if any.*/
	private var verticeView: VerticeView<*>? = null

	/** Contains the [Point2D]s where a connection is currently possible.*/
	private val points = mutableListOf<Point2D>()

	init {
		eventBus.register(DragEvent::class, { handleDragEvent(it) })
		eventBus.register(DropEvent::class, { handleDropEvent(it) })

		editor.view.addMouseListener(object : MouseAdapter() {
			override fun mouseReleased(e: MouseEvent) {
				if (verticeView != null) {
					removeHighlight()
					connectPorts()
					verticeView = null
				}
			}
		})
	}

	/** ---- [AutoConnector] */

	private fun handleDragEvent(event: DragEvent) {
		if (event.editor != editor) {
			return
		}
		if (event.components.size != 1) {
			return
		}
		if (event.components.first() !is VerticeView<*>) {
			return
		}

		verticeView = event.components.first() as VerticeView<*>

		matchPoints()

		if (points.size > 0) {
			if (!isHighlightDisplayed) {
				highlight.setPoints(points)
				addHighlight()
			}
		} else {
			if (isHighlightDisplayed) {
				removeHighlight()
			}
		}
	}

	private fun handleDropEvent(event: DropEvent) {
		removeHighlight()
		connectPorts()
	}

	/**
	 * Fills the connection point locations of all [PortView]s of the current [VerticeView] that match
	 * an [EdgeView] endpoint into [points].
	 */
	private fun matchPoints() {
		points.clear()
		(editor.drawing as GraphView).getEdgeViews()
			.filter { it.origin == null || it.destination == null }
			.forEach { ev ->
				verticeView!!.getPortViews().forEach {
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

	private fun connectPorts() {
		(editor.drawing as GraphView).getEdgeViews()
			.filter { it.origin == null || it.destination == null }
			.forEach { ev ->
				verticeView!!.getPortViews().forEach {
					val p = it.owner!!.getPortConnectionPoint(it.port)
					if (ev.origin == null && it.port.portType.isOutput && p == ev.originEndpointView.location) {
						connectService.connectToOrigin(ev, it.createConnection() as Connection<Any>)
					}
					if (ev.destination == null && it.port.portType.isInput && p == ev.destinationEndpointView.location) {
						connectService.connectToDestination(ev, it.createConnection() as Connection<Any>)
					}
				}
			}
	}

	private fun addHighlight() {
		editor.view.ghostContainer.add(highlight)
		highlight.validate()
		isHighlightDisplayed = true
	}

	private fun removeHighlight() {
		editor.view.ghostContainer.remove(highlight)
		editor.drawing.validate()
		isHighlightDisplayed = false
	}
}