package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistory
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import kotlin.math.max

/** Draws a [SignalHistoryTimeline] as a single row of an [OscilloscopeView].*/
interface SignalHistoryTimelineView : RectangularDrawable {

	/**
	 * Binds this [SignalHistoryTimelineView] at start of execution with relevant runtime
	 * information.
	 *
	 * @param gridSignalHistory the [SignalHistory] whose signal changes determine the location
	 * of the vertical grid lines
	 */
	fun bind(
		gridSignalHistory: SignalHistory<Any>?,
		timeline: SignalHistoryTimeline?
	)

	/**
	 * Returns the simulation time corresponding with an x-coordinate on this [SignalHistoryTimelineView].
	 * Can be used e.g. for displaying a tooltip with time/value at that coordinate.
	 */
	fun getTime(x: Double): SignalHistoryTimelineTime

	/**
	 * Called by [OscilloscopeView] after new signals have arrived to ask this [SignalHistoryTimelineView]
	 * to update its scale, if necessary.
	 */
	fun updateGeometry()
}

data class SignalHistoryTimelineTime(
	val absoluteTime: Long,
	val relativeTime: Long
)

/** Draws a ruler-like timeline. */
class SignalHistoryTimelineViewImpl(
	private val rightInset: Int
) : AbstractRectangle(), SignalHistoryTimelineView {

	companion object {
		private const val LINE_LENGTH = 4.0
		private const val LABEL_INSET = 3.0
		private const val MIN_LABEL_GAP = 10
	}

	/**
	 * The [SignalHistory] whose signal times determine the locations of the vertical grid lines
	 * and therefore also of the scale markings.
	 */
	private var gridSignalHistory: SignalHistory<Any>? = null

	/** The [SignalHistoryTimeline] used for mapping signal time to horizontal model coordinates.*/
	private var timeline: SignalHistoryTimeline? = null

	/** A flyweight [Label] for drawing the various timestamps.*/
	private val label = Label(
		text = "",
		font = Themes.get<GraphTheme>().annotation.font,
		color = Themes.get<GraphTheme>().annotation.color.textColor,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.TOP)

	private val rightBorder: Double get() = bounds.x + bounds.width - rightInset

	private var minDisplayableTime: Long = 0

	/** ---- [SignalHistoryTimelineView] */

	override fun bind(gridSignalHistory: SignalHistory<Any>?, timeline: SignalHistoryTimeline?) {
		this.gridSignalHistory = gridSignalHistory
		this.timeline = timeline
	}

	override fun getTime(x: Double): SignalHistoryTimelineTime {
		val time = timeline!!.getTime(rightBorder - x)
		return SignalHistoryTimelineTime(time, time - minDisplayableTime)
	}

	override fun updateGeometry() {
		updateMinDisplayableTime()
	}

	/** ---- [Drawable] */

	override fun draw(context: DrawContext) {
		if (timeline == null || gridSignalHistory == null) {
			return
		}

		context.g.color = Themes.get<GraphTheme>().annotation.color.foregroundColor
		context.g.font = Themes.get<GraphTheme>().annotation.font

		val refTime = minDisplayableTime
		var lastLabelMinX: Double? = null

		for (entry in gridSignalHistory!!.getReverseEntriesUntil(0)) {
			lastLabelMinX = drawLabel(context, entry.time, refTime, lastLabelMinX)
		}

		drawLabel(context, timeline!!.maxTime, refTime, lastLabelMinX)
	}

	private fun drawLabel(context: DrawContext, time: Long, refTime: Long, lastLabelMinX: Double?): Double? {
		var newLastLabelMinX = lastLabelMinX

		val x = max(rightBorder - timeline!!.getX(time), bounds.minX)
		if (x <= bounds.minX) {
			//break
			return null
		}

		label.text = (time - refTime).toString()
		label.location = Point2D(x, LINE_LENGTH + LABEL_INSET)

		if (newLastLabelMinX == null || label.boundingBox.maxX + MIN_LABEL_GAP < newLastLabelMinX) {
			newLastLabelMinX = label.boundingBox.minX
			context.g.drawLine(x, 0.0, x, LINE_LENGTH)
			label.draw(context)
		}

		return newLastLabelMinX
	}

	override val lineWidth: Double get() = 0.0

	/** ---- [SignalHistoryTimelineViewImpl] */

	/**
	 * The smallest effective simulation time that can be displayed to the left side of the view.
	 * This is then displayed as "0"", and all subsequent time marks are relative to this one.
	 */
	private fun updateMinDisplayableTime() {
		var time = timeline!!.maxTime

		if (gridSignalHistory == null) {
			minDisplayableTime = time
			return
		}

		val entries = gridSignalHistory!!.getReverseEntriesUntil(0)
		if (!entries.hasNext()) {
			minDisplayableTime = time
			return
		}

		do {
			val entry = entries.next()
			if ((rightBorder - timeline!!.getX(entry.time)) < bounds.minX) {
				minDisplayableTime = time
				return
			}
			time = entry.time

		} while (entries.hasNext())

		minDisplayableTime = time
	}
}