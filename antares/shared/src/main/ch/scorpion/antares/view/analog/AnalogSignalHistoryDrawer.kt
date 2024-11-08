package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.jabbah.graph.view.oscilloscope.AbstractSignalHistoryDrawer
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryYAxis
import kotlin.math.max

class AnalogSignalHistoryDrawer(
	rightInset: Int,
	yAxis: SignalHistoryYAxis<AnalogSignal>
): AbstractSignalHistoryDrawer<AnalogSignal>(rightInset, Themes.get<AntaresTheme>().screen, yAxis) {

	companion object {
		const val ROW_HEIGHT = 100
	}

	/** ---- [AbstractSignalHistoryDrawer] */

	override val gridEnabled: Boolean get() = false

	override fun signalY(entry: SignalHistoryEntry<AnalogSignal>): Double =
		yAxis!!.baselineY + yAxis!!.signalY(entry.signal)

	override val signalHeight: Double get() = yAxis!!.signalHeight

	override fun drawCurve(context: DrawContext) {
		var lastPoint = Point2D.ZERO
		var lastEntry: SignalHistoryEntry<AnalogSignal>? = null
		var effNextX: Double = rightBorder
		context.g.stroke = CURVE_STROKE

		yAxis!!.setMinMax(signalHistory?.minimum, signalHistory?.maximum)

		for (entry in signalHistory!!.getReverseEntriesUntil(0)) {
			val x = rightBorder - timeline!!.getX(entry.time)
			val y = signalY(entry)
			if (lastEntry == null) {
				// Right border
				lastPoint = Point2D(x, y)
				effNextX = max(x, bounds.minX)

				drawRightBorder(context, effNextX, y)

				if (x <= bounds.minX) {
					break
				}
			} else {
				val nextX = x
				val nextY = y
				effNextX = max(nextX, bounds.minX)

				//drawNonHorizontalSegment(context, lastPoint.x, lastPoint.y, effNextX, nextY)
				drawHorizontalSegment(context, lastPoint.x, lastPoint.y, effNextX, nextY)

				if (nextX <= bounds.minX) {
					break
				}

				lastPoint = Point2D(effNextX, nextY)
			}

			lastEntry = entry
		}

		lastEntry?.let {
			if (signalHistory!!.overflow && effNextX - BUFFER_END_WIDTH > bounds.minX) {
				drawBufferEnd(context, effNextX)
			}
		}
	}
}