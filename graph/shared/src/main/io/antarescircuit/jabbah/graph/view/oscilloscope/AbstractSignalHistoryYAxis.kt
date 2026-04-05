package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import kotlin.math.abs
import kotlin.math.min

/**
 * Default implementation of the [SignalHistoryYAxis] interface.
 *
 * @param topInset the distance from the [AbstractRectangle]'s top edge to the drawing area
 * @param bottomInset the distance from the [AbstractRectangle]'s bottom edge to the drawing area
 * @param defaultValue the value to be displayed at [defaultValueTopInset]
 * @param defaultValueTopInset the additional distance from [topInset] at which the default signal value
 * is to be drawn. This [AbstractSignalHistoryYAxis] keeps the scale factor stable as long as all
 * signal values are smaller than the default value
 */
abstract class AbstractSignalHistoryYAxis<T: Any>(
	private val topInset: Int,
	private val bottomInset: Int,
	defaultValue: T,
	private val defaultValueTopInset: Int,
	protected val color: CompositeColor
) : AbstractRectangle(), SignalHistoryYAxis<T> {

	companion object {
		const val DEF_TOP_INSET = 6
		const val DEF_BOTTOM_INSET = 2
		const val DEF_DEFAULT_VALUE_TO_INSET = 20
		private const val SCALE_WIDTH = 5
	}

	private var factor: Double = 1.0

	private val label = Label(
		"$defaultValue",
		Themes.get<GraphTheme>().annotation.font,
		Themes.get<GraphTheme>().figure.color.textColor,
		HorizontalAlignment.LEFT,
		VerticalAlignment.CENTER)

	protected var defaultValue: T = defaultValue
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				label.text = field.toString()
				updateScaling()
				invalidate()
				validate()
			}
		}

	/** ---- [AbstractRectangle] */

	override fun draw(context: DrawContext) {
		drawAxisLine(context)
		drawRuler(context)
	}

	override fun setBounds(x: Double, y: Double, w: Double, h: Double) {
		super<AbstractRectangle>.setBounds(x, y, w, h)
		updateScaling()
	}

	/** ---- [SignalHistoryYAxis] interface */

	private var min: T? = null
	private var max: T? = null

	private val availableHeight get() = height - topInset - defaultValueTopInset - bottomInset

	private val defaultFactor get() = availableHeight / toMetric(defaultValue)

	override var baselineY: Double = 0.0

	override val signalHeight: Double get() =
		if (min == null || max == null) {
			defaultFactor * toMetric(defaultValue)
		} else {
			factor * abs(toMetric(max!!) - toMetric(min!!))
		}

	override fun setMinMax(min: T?, max: T?) {
		this.min = min
		this.max = max
		updateScaling()
	}

	override fun signalY(signal: T): Double = -factor * toMetric(signal)

	private fun drawAxisLine(context: DrawContext) {
		context.g.color = color.foregroundColor
		context.g.drawLine(bounds.minX, bounds.minY + topInset, bounds.minX, bounds.maxY - bottomInset)
	}

	private fun drawRuler(context: DrawContext) {
		val defaultValueY = baselineY + signalY(defaultValue)
		context.g.color = color.foregroundColor
		context.g.stroke = Themes.get<GraphTheme>().annotation.stroke
		context.g.drawLine(bounds.minX, defaultValueY, bounds.minX - SCALE_WIDTH, defaultValueY)

		label.location = Point2D(bounds.minX + SCALE_WIDTH, defaultValueY)
		label.draw(context)
	}

	private fun updateScaling() {
		baselineY = bounds.maxY - bottomInset
		if (min == null || max == null) {
			factor = defaultFactor
			return
		}

		val maxDouble = toMetric(max!!)
		val minDouble = toMetric(min!!)
		var h = 0.0
		if (maxDouble > 0) {
			h += maxDouble
			if (minDouble < 0) {
				h += abs(minDouble)
			}
		} else {
			h = abs(minDouble)
		}

		factor = min(defaultFactor, availableHeight / h)
		baselineY = if (minDouble >= 0) {
			bounds.maxY - bottomInset
		} else {
			bounds.maxY - bottomInset - factor * abs(minDouble)
		}
	}
}