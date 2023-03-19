package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.DrawableButton
import ch.scorpion.jabbah.draw.drawable.IconDrawableButtonRenderer
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColor
import ch.scorpion.jabbah.draw.graphics.RemoveIcon
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.app.oscilloscope.OscilloscopeViewService
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.DRAWER_X

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
) : DrawableContainerImpl<Drawable>(location = initLocation, useLocation = true) {

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
	}

	var name: String
		get() = probeView.name
		set(value) {
			probeView.name = value
		}

	fun updateGeometry() {
		drawer.setBounds(DRAWER_X, 0.0, oscilloscopeView.drawerWidth, oscilloscopeView.rowHeight.toDouble())
		yAxis?.setBounds(DRAWER_X + oscilloscopeView.drawerWidth, 0.0, yAxis.width, oscilloscopeView.rowHeight.toDouble())
	}

	fun updateState() {
		removeButton.enabled = oscilloscopeView.applicationMode.isEdit()
	}

	fun loadedWith(vertice: OscilloscopeProbeVerticeView<Any>) {
		probeView.verticeView = vertice
		probeView.verticeView!!.refColor = color.onBackground
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
}