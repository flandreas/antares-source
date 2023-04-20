package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.view.style.GraphTheme
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
}

/** Draws a ruler-like timeline. */
class SignalHistoryTimelineViewImpl(
	private val rightInset: Int
) : AbstractRectangle(), SignalHistoryTimelineView {

	companion object {
		private const val LINE_LENGTH = 4.0
		private const val LABEL_INSET = 3.0
		private const val MIN_LABEL_GAP = 10
	}

	/** The [SignalHistory] whose signal times determine the locations of the vertical grid lines.*/
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

	/** ---- [SignalHistoryTimelineView] */

	override fun bind(gridSignalHistory: SignalHistory<Any>?, timeline: SignalHistoryTimeline?) {
		this.gridSignalHistory = gridSignalHistory
		this.timeline = timeline
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

	private val minDisplayableTime: Long get() {
		var time = timeline!!.maxTime
		val entries = gridSignalHistory!!.getReverseEntriesUntil(0)

		if (!entries.hasNext()) {
			return time
		}

		do {
			val entry = entries.next()
			if ((rightBorder - timeline!!.getX(entry.time)) < bounds.minX) {
				return time
			}
			time = entry.time

		} while (entries.hasNext())

		return time
	}
}