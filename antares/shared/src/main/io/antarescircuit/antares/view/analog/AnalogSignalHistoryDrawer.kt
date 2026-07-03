package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.jabbah.graph.view.oscilloscope.AbstractSignalHistoryDrawer
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import io.antarescircuit.jabbah.graph.view.oscilloscope.SignalHistoryYAxis
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
			val x = rightBorder - timeline!!.getX(entry.time) + scrollX
			val y = signalY(entry)
			if (lastEntry == null) {
				// Right border
				lastPoint = Point2D(x, y)
				effNextX = max(x, bounds.minX)

				if (effNextX <= rightBorder) {
					drawRightBorder(context, effNextX, y)
				}

				if (x <= bounds.minX) {
					break
				}
			} else {
				val nextX = x
				val nextY = y
				effNextX = max(nextX, bounds.minX)

				if (effNextX < rightBorder) {
					drawSegment(context, lastPoint.x, lastPoint.y, effNextX, nextY)
				}

				if (effNextX <= bounds.minX) {
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