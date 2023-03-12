package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.oscilloscope.AbstractAntaresSignalHistoryDrawer
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import kotlin.math.max

class AnalogSignalHistoryDrawer : AbstractAntaresSignalHistoryDrawer<AnalogSignal>() {

	companion object {
		const val ROW_HEIGHT = 60
	}

	/** ---- [AbstractAntaresSignalHistoryDrawer] */

	override fun signalY(entry: SignalHistoryEntry<AnalogSignal>): Double {
		return baseLineY - 8 * entry.signal.voltage
	}

	override val signalHeight: Double get() = 20.0

	override fun drawCurve(context: DrawContext) {
		var lastPoint = Point2D.ZERO
		var lastEntry: SignalHistoryEntry<AnalogSignal>? = null
		var effNextX: Double = rightBorder
		context.g.stroke = CURVE_STROKE

		for (entry in signalHistory!!.getReverseEntriesUntil(0)) {
			val x = rightBorder - timeline!!.getX(entry.time)
			val y = signalY(entry)
			if (lastEntry == null) {
				// Right border
				lastPoint = Point2D(x, y)
				effNextX = max(x, bounds.minX)

				drawSingleBitRightBorder(context, effNextX, y)

				if (x <= bounds.minX) {
					break
				}
			} else {
				val nextX = x
				val nextY = y
				effNextX = max(nextX, bounds.minX)

				drawSingleBitSegment(context, lastPoint.x, lastPoint.y, effNextX, nextY)

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