package io.antarescircuit.jabbah.graph.view.app.oscilloscope

import io.antarescircuit.jabbah.base.collection.ImmutableList
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView

/**
 * Application-level services related with [OscilloscopeView].
 *
 * Displaying an [OscilloscopeView] in a [GraphView] is a non-persistent property. When displaying an [OscilloscopeView],
 * the visibility property of the [OscilloscopeView] and its [OscilloscopeProbeVerticeView]s is set to `true`, and
 * these properties are not stored.
 */
interface OscilloscopeViewService {

	/**
	 * Determines whether the [OscilloscopeView] is currently displayed in the specified [GraphView].
	 * That property is not explicitly stored in [GraphView]. It is rather controlled by
	 * the visibility property of [OscilloscopeView] and the corresponding [OscilloscopeProbeVerticeView]s.
	 */
	fun isOscilloscopeDisplayed(graphView: GraphView): Boolean

	/**
	 * Displays the [OscilloscopeView] in the specified [GraphView].
	 * Does nothing if it is already displayed. Creates an instance of [OscilloscopeView] if necessary
	 * and no one exists yet, and positions it just below the bounding box of the [GraphView]'s contents. Posts an
	 * [OscilloscopeDisplayEvent] on [EventBus] if displaying of the [OscilloscopeView] has been changed.
	 */
	fun displayOscilloscope(view: DrawingView<GraphElementView<*>, GraphView>)

	/**
	 * Hides the [OscilloscopeView] in the specified [GraphView].
	 * Does nothing if it is not displayed. Posts an [OscilloscopeDisplayEvent] on [EventBus].
	 */
	fun hideOscilloscope(view: DrawingView<GraphElementView<*>, GraphView>)

	/** Adds a new row at the end of the specified [OscilloscopeView]*/
	fun addRow(view: DrawingView<*,*>, oscilloscopeView: OscilloscopeView)

	/** Removes a row from the specified [OscilloscopeView].*/
	fun removeRow(view: DrawingView<*,*>, name: String, oscilloscopeView: OscilloscopeView)

	/**
	 * Sets the visibility of an existing [OscilloscopeView] to the specified value
	 * WITHOUT issuing a [Command], because this method is typically used from within
	 * a [Command] issued by [displayOscilloscope] or [hideOscilloscope].
	 * Does nothing if the [OscilloscopeView] doesn't yet exist.
	 */
	fun setOscilloscopeViewVisibility(view: DrawingView<GraphElementView<*>, GraphView>, visible: Boolean)

	/**
	 * Drops a newly detached [OscilloscopeProbeVerticeView] into a [GraphView], or moves an
	 * already existing one within the [GraphView].
	 *
	 * @return the ID of the [io.antarescircuit.jabbah.graph.view.EdgeView] to which the [OscilloscopeProbeVerticeView]
	 * got connected, if any
	 */
	fun <T : Any> dropProbe(
		view: DrawingView<GraphElementView<*>, GraphView>,
		name: String,
		location: Point2D,
		probeVerticeViewId: Int?
	): Int?

	/**
	 * Called when an [OscilloscopeView] has been deleted from a [DrawingView].
	 */
	fun handleOscilloscopeDeleted(graphView: GraphView)
}

/**
 * Posted by [OscilloscopeViewService] on [EventBus] when the displaying of [OscilloscopeView] has changed
 * in a particular [GraphView].
 */
data class OscilloscopeDisplayEvent(val graphView: GraphView)

/**
 * An application service dealing with [OscilloscopeView].
 */
class OscilloscopeViewServiceImpl(
	private val graphViewAppService: GraphViewAppService = GraphViewModule.graphViewAppService,
	private val commandManager: CommandManager = EditModule.commandManager,
	private val eventBus: EventBus = BaseModule.eventBus
) : OscilloscopeViewService {

	companion object {
		private val LOG by logger(OscilloscopeViewServiceImpl::class)

		/** The vertical distance between the [GraphView]'s bounding box when positioning [OscilloscopeView].*/
		private const val DISTANCE = 20.0
	}

	/** ---- [OscilloscopeViewService] */

	override fun isOscilloscopeDisplayed(graphView: GraphView): Boolean {
		val ov = findOscilloscopeView(graphView)
		return ov != null && ov.visible
	}

	override fun displayOscilloscope(view: DrawingView<GraphElementView<*>, GraphView>) {
		if (findOscilloscopeView(view.drawing) == null) {
			LOG.userTrail("Display Oscilloscope by creating")
			createOscilloscopeView(view)
		} else {
			LOG.userTrail("Display Oscilloscope by making visible")
			commandManager.execute(OscilloscopeVisibilityCommand(view, true))
		}
		eventBus.post(OscilloscopeDisplayEvent(view.drawing))
	}

	override fun hideOscilloscope(view: DrawingView<GraphElementView<*>, GraphView>) {
		findOscilloscopeView(view.drawing)?.let {
			LOG.userTrail("Hiding Oscilloscope")
			commandManager.execute(OscilloscopeVisibilityCommand(view, false))
		}
	}

	override fun setOscilloscopeViewVisibility(view: DrawingView<GraphElementView<*>, GraphView>, visible: Boolean) {
		findOscilloscopeView(view.drawing)?.let { ov ->
			if (oscilloscopeViewOverlapsCircuit(ov, view.drawing)) {
				positionOscilloscope(ov, view.drawing)
			}
			ov.visible = visible
			findProbeViews(view.drawing).forEach { it.visible = visible }
			eventBus.post(OscilloscopeDisplayEvent(view.drawing))
		}
	}

	override fun addRow(view: DrawingView<*,*>, oscilloscopeView: OscilloscopeView) {
		LOG.userTrail("Add Oscilloscope row")
		commandManager.execute(AddOscilloscopeRowCommand(view, oscilloscopeView.id))
	}

	override fun removeRow(view: DrawingView<*,*>, name: String, oscilloscopeView: OscilloscopeView) {
		LOG.userTrail("Remove Oscilloscope row")
		commandManager.execute(RemoveOscilloscopeRowCommand(view, name, oscilloscopeView.id))
	}

	override fun <T : Any> dropProbe(
		view: DrawingView<GraphElementView<*>, GraphView>,
		name: String,
		location: Point2D,
		probeVerticeViewId: Int?
	): Int? {
		val oscilloscopeView = findOscilloscopeView(view.drawing)!!
		val signalRowView = oscilloscopeView.rowWithName(name)!!
		val probeVerticeView = if (probeVerticeViewId == null) {
			GraphViewModule.oscilloscopeViewFactory.createProbeVerticeView<Any>(
				signalRowView.name,
				view.drawing.graph!!.type,
				signalRowView.color.onBackground
			).also {
				it.location = location
				it.visible = true
				view.drawing.add(it)
			}
		} else {
			view.drawing.getWithId(probeVerticeViewId) as OscilloscopeProbeVerticeView<T>
		}

		// Must be done before setting the name, since Name event handling depends on
		// OscilloscopeProbeVerticeView being set in OscilloscopeProbeView
		signalRowView.probeView.handleProbeViewAddedToDrawing(probeVerticeView)

		// Unconnect from old Net
		val newEdgeView = view.drawing.getEdgeViews().firstOrNull { it.contains(probeVerticeView.connectionPoint()) }
		if (probeVerticeView.model.getPort<T>().isConnected) {
			probeVerticeView.model.getPort<T>().net!!.unconnect(probeVerticeView.model.getPort<T>())
			probeVerticeView.edgeView = null
		}

		// Connect to new Net
		if (newEdgeView != null) {
			probeVerticeView.edgeView = newEdgeView
			(newEdgeView.model as Net<T>).connect(probeVerticeView.model.getPort())
			GraphViewModule.verticeViewNameStrategy.getConnectedName(oscilloscopeView.model, probeVerticeView.model, newEdgeView)?.let {
				probeVerticeView.name = it
			}
		}

		return newEdgeView?.id
	}

	override fun handleOscilloscopeDeleted(graphView: GraphView) {
		eventBus.post(OscilloscopeDisplayEvent(graphView))
	}

	/** ---- [OscilloscopeViewServiceImpl] */

	private fun findOscilloscopeView(graphView: GraphView): OscilloscopeView? =
		graphView.getDrawable { it is OscilloscopeView } as OscilloscopeView?

	private fun findProbeViews(graphView: GraphView): ImmutableList<Component> =
		graphView.getDrawables { it is OscilloscopeProbeVerticeView<*> }

	/** Positions [OscilloscopeView] right beneath [GraphView]'s bounding box.*/
	private fun positionOscilloscope(ov: OscilloscopeView, graphView: GraphView) {
		val bbox = graphView.boundingBox
		ov.location = Point2D(bbox.centerX - ov.width / 2, bbox.maxY + DISTANCE)
	}

	private fun createOscilloscopeView(view: DrawingView<GraphElementView<*>, GraphView>) {
		val ov = OscilloscopeView(view.drawing.graph!!.type)
		ov.mode = GraphViewModule.oscilloscopeViewFactory.getDefaultMode(view.drawing.graph!!.type)
		ov.addRow()
		ov.visible = true
		positionOscilloscope(ov, view.drawing)
		graphViewAppService.add(ov, view as DrawingView<Component, Drawing<Component>>)
	}

	private fun oscilloscopeViewOverlapsCircuit(ov: OscilloscopeView, graphView: GraphView): Boolean {
		val bbox = ov.boundingBox
		return graphView.drawables
			.filter { it !== ov }
			.any { it.intersects(bbox) }
	}
}



