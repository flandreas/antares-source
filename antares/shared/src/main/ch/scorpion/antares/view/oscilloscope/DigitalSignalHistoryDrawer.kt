package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import kotlin.math.max

class DigitalSignalHistoryDrawer : AbstractAntaresSignalHistoryDrawer<DigitalSignal>() {

	companion object {

		/** The height of an individual row.*/
		const val ROW_HEIGHT: Int = 40

		/** The horizontal inset used when drawing the arrow head of a multi-bit signal curve.*/
		private const val MULTIBIT_INSET = 3.0


	}

	/** Uses for drawing the signal value of a multi-bit [DigitalSignal].*/
	private val multiBitLabel = Label(
		text = "",
		font = Themes.get<GraphTheme>().annotation.font,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER
	)


	/** ---- [DigitalSignalHistoryDrawer] */

	override fun drawCurve(context: DrawContext) {
		val singleBit = signalHistory!!.last().signal.bitWidth.width == 1
		var lastPoint = Point2D.ZERO
		var lastEntry: SignalHistoryEntry<DigitalSignal>? = null
		var effNextX: Double = rightBorder
		context.g.stroke = CURVE_STROKE

		for (entry in signalHistory!!.getReverseEntriesUntil(0)) {
			val x = rightBorder - timeline!!.getX(entry.time)
			val y = signalY(entry)
			if (lastEntry == null) {
				// Right border
				lastPoint = Point2D(x, y)
				effNextX = max(x, bounds.minX)

				if (singleBit) {
					drawSingleBitRightBorder(context, effNextX, y)
				} else {
					multiBitLabel.text = entry.signal.hexString
					multiBitLabel.horizontalAlignment = HorizontalAlignment.LEFT
					multiBitLabel.location = Point2D(effNextX + MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT / 2)
					drawMultiBitRightBorder(context, effNextX)
				}

				if (x <= bounds.minX) {
					break
				}
			} else {
				val nextX = x
				val nextY = y
				effNextX = max(nextX, bounds.minX)

				if (singleBit) {
					drawSingleBitSegment(context, lastPoint.x, lastPoint.y, effNextX, nextY)
				} else {
					multiBitLabel.text = entry.signal.hexString
					multiBitLabel.horizontalAlignment = HorizontalAlignment.CENTER
					multiBitLabel.location = Point2D(effNextX + (lastPoint.x - effNextX) / 2, baseLineY - SIGNAL_HEIGHT / 2)
					drawMultiBitSegment(context, xR = lastPoint.x, xL = effNextX, first = false)
				}

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

	private fun drawMultiBitRightBorder(context: DrawContext, xL: Double) {
		if (xL <= rightBorder - START_SIZE - MULTIBIT_INSET) {
			drawMultiBitSegment(context, xR = rightBorder, xL = xL, first = true)
		}

		context.g.fillOval(rightBorder - START_SIZE, baseLineY - SIGNAL_HEIGHT / 2 - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)

		multiBitLabel.color = if (fillSignal) color!!.textColor else Themes.get<GraphTheme>().annotation.color.textColor
		multiBitLabel.draw(context)
	}

	private fun drawMultiBitSegment(context: DrawContext, xR: Double, xL: Double, first: Boolean) {
		if (fillSignal) {
			context.g.color = color!!.backgroundColor
			context.g.fill(multiBitSegmentPath(xR, xL, fill = true, first))
		}
		context.g.color = color!!.foregroundColor
		context.g.draw(multiBitSegmentPath(xR, xL, fill = false, first))

		multiBitLabel.color = if (fillSignal) color!!.textColor else Themes.get<GraphTheme>().annotation.color.textColor
		multiBitLabel.draw(context)
	}

	private fun multiBitSegmentPath(xR: Double, xL: Double, fill: Boolean, first: Boolean): Path {
		val path = System.createPath()

		if (first) {
			path
				.moveTo(xR, baseLineY - SIGNAL_HEIGHT)
		} else {
			path
				.moveTo(xR, baseLineY - SIGNAL_HEIGHT / 2)
				.lineTo(xR - MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT)
		}

		if (xL > bounds.minX) {
			path
				.lineTo(xL + MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT)
				.lineTo(xL, baseLineY - SIGNAL_HEIGHT / 2)
				.lineTo(xL + MULTIBIT_INSET, baseLineY)
		} else {
			path
				.lineTo(xL, baseLineY - SIGNAL_HEIGHT)
			if (fill) {
				path
					.lineTo(xL, baseLineY)
			} else {
				path
					.moveTo(xL, baseLineY)
			}
		}

		if (first) {
			path
				.lineTo(xR, baseLineY)
		} else {
			path
				.lineTo(xR - MULTIBIT_INSET, baseLineY)
				.lineTo(xR, baseLineY - SIGNAL_HEIGHT / 2)
		}

		return path
	}

	override fun signalY(entry: SignalHistoryEntry<DigitalSignal>): Double {
		return if (entry.signal.bitAt(0).isSet) {
			baseLineY - SIGNAL_HEIGHT
		} else {
			baseLineY
		}
	}
}