package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.drawable.DrawableButton
import io.antarescircuit.jabbah.draw.drawable.IconDrawableButtonRenderer
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.ReferenceColor
import io.antarescircuit.jabbah.draw.graphics.RemoveIcon
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.execution.actor.ActorViewContainer
import io.antarescircuit.jabbah.graph.view.app.oscilloscope.OscilloscopeViewService
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.DRAWER_X

/**
 * Part of an [OscilloscopeView].
 *
 * Displays an individual [SignalHistoryDrawer], a button for removing this [OscilloscopeSignalRowView]
 * from the [OscilloscopeView], and a [OscilloscopeProbeView] to be used for dragging the probe into
 * the drawing.
 */
class OscilloscopeSignalRowView(
	private val oscilloscopeView: OscilloscopeView,
	name: String,
	initLocation: Point2D,
	val color: ReferenceColor,
	private val service: OscilloscopeViewService,
	private val drawer: SignalHistoryDrawer<Any>,
	val yAxis: SignalHistoryYAxis<*>?
) : ActorViewContainer<Drawable>(location = initLocation, useLocation = true) {

	companion object {
		private val NON_INDIVIDUAL_SIGNAL_COLOR = CompositeColor(Color.LIGHT_GRAY, Color.DARK_GRAY)
	}

	val probeView = OscilloscopeProbeView(
		location = Point2D(2.0 * OscilloscopeView.ROW_INSET + OscilloscopeView.ICON_BUTTON_SIZE, oscilloscopeView.rowHeight / 2 - OscilloscopeProbeViewIcon.SIZE / 2),
		name = name,
		probeColor = color.onBackground,
		origLocSource = { oscilloscopeView.location.add(location) })

	private val removeButton = DrawableButton<EditInputEventContext>(
		renderer = IconDrawableButtonRenderer(RemoveIcon(Dimension2D(OscilloscopeView.ICON_BUTTON_SIZE, OscilloscopeView.ICON_BUTTON_SIZE))),
		tooltipKey = "graph.action.oscilloscope.removeRow.name",
		location = Point2D(OscilloscopeView.ROW_INSET, oscilloscopeView.rowHeight / 2 - OscilloscopeView.ICON_BUTTON_SIZE / 2),
		action = { service.removeRow(it.drawingView, probeView.name, oscilloscopeView) },
		styleType = StyleType.ANNOTATION)

	init {
		add(removeButton)
		add(probeView)
		add(drawer)
		if (yAxis != null) {
			add(yAxis)
		}
		updateState()
	}

	override fun dispose() {
		super.dispose()
		yAxis?.dispose()
	}

	var name: String
		get() = probeView.name
		set(value) {
			probeView.name = value
		}

	/** The distance (in model coordinate space) in x direction the view has been scrolled by the user.*/
	var scrollX: Double = 0.0
		set(value) {
			field = value
			drawer.scrollX = value
		}

	fun updateGeometry() {
		drawer.setBounds(DRAWER_X, 0.0, oscilloscopeView.drawerWidth, oscilloscopeView.rowHeight.toDouble())
		yAxis?.setBounds(DRAWER_X + oscilloscopeView.drawerWidth, 0.0, yAxis.preferredWidth.toDouble(), oscilloscopeView.rowHeight.toDouble())
	}

	fun updateState() {
		(oscilloscopeView.applicationMode.isEdit() && oscilloscopeView.editable).let {
			removeButton.enabled = it
			probeView.enabled = it
		}
	}

	fun loadedWith(vertice: OscilloscopeProbeVerticeView<*>) {
		probeView.verticeView = vertice
		probeView.verticeView!!.refColor = color.onBackground
		yAxis?.loadedWith(vertice.model)
	}

	fun handleProbeViewRemovedFromDrawing() {
		probeView.handleProbeViewRemovedFromDrawing()
	}

	fun bindDrawer() {
		val signalColor = if (BaseModule.properties.getBoolean(OscilloscopeView.PROP_INDIVIDUAL_PROBE_COLORS)) {
			color.onDark.withAlpha(164)
		} else {
			NON_INDIVIDUAL_SIGNAL_COLOR
		}

		drawer.bind(
			oscilloscopeView.model.getSignalHistory(name),
			oscilloscopeView.signalRowViews.firstOrNull()?.let { oscilloscopeView.model.getSignalHistory(it.name) },
			oscilloscopeView.timeline,
			signalColor
		)
	}

	fun unbindDrawer() {
		drawer.bind(null, null, null, color.onDark)
	}

	private var tooltipLocation = Rectangle2D()

	override fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip {
		val time = oscilloscopeView.scaleRowView.timelineView.getTime(context.x - scrollX)
		val entry = oscilloscopeView.model.getSignalHistory(name)!!.getEntryAt(time.absoluteTime)

		val absMouse = toAbsoluteLocation(context.location)
		tooltipLocation.x = absMouse.x
		tooltipLocation.y = absMouse.y

		return Tooltip("${time.relativeTime} ns: ${entry?.signal ?: "?"}", tooltipLocation)
	}
}