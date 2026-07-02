package io.antarescircuit.antares.view.oscilloscope

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Path
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import io.antarescircuit.jabbah.graph.view.oscilloscope.AbstractSignalHistoryDrawer
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import kotlin.math.max

class DigitalSignalHistoryDrawer(
	rightInset: Int
): AbstractSignalHistoryDrawer<DigitalSignal>(rightInset, Themes.get<AntaresTheme>().screen, null) {

	companion object {

		/** The height of an individual row.*/
		const val ROW_HEIGHT: Int = 40

		/** The horizontal inset used when drawing the arrow head of a multi-bit signal curve.*/
		private const val MULTI_BIT_INSET = 3.0
	}

	/** Uses for drawing the signal value of a multi-bit [DigitalSignal].*/
	private val multiBitLabel = Label(
		text = "",
		font = Themes.get<GraphTheme>().annotation.font,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER
	)

	override val signalHeight: Double get() = 20.0

	/** ---- [DigitalSignalHistoryDrawer] */

	override val gridEnabled: Boolean get() = true

	private val clipBuffer = Rectangle2D()

	override fun drawCurve(context: DrawContext) {
		val singleBit = signalHistory!!.last().signal.bitWidth.width == 1
		var lastPoint = Point2D.ZERO
		var lastEntry: SignalHistoryEntry<DigitalSignal>? = null
		var effNextX: Double = rightBorder
		context.g.stroke = CURVE_STROKE

		context.g.getClipBounds(clipBuffer)
		context.g.setClipBounds(bounds.xInt, bounds.yInt, bounds.widthInt - rightInset + START_SIZE.toInt(), bounds.heightInt)

		for (entry in signalHistory!!.getReverseEntriesUntil(0)) {
			val x = rightBorder - timeline!!.getX(entry.time) + scrollX
			val y = signalY(entry)
			if (lastEntry == null) {
				// Right border
				lastPoint = Point2D(x, y)
				effNextX = max(x, bounds.minX)

				if (effNextX <= rightBorder) {
					if (singleBit) {
						drawRightBorder(context, effNextX, y)
					} else {
						multiBitLabel.text = entry.signal.hexString
						multiBitLabel.horizontalAlignment = HorizontalAlignment.LEFT
						multiBitLabel.location = Point2D(effNextX + MULTI_BIT_INSET, baseLineY - signalHeight / 2)
						drawMultiBitRightBorder(context, effNextX)
					}
				}

				if (x <= bounds.minX) {
					break
				}
			} else {
				val nextX = x
				val nextY = y
				effNextX = max(nextX, bounds.minX)

				if (effNextX < rightBorder) {
					if (singleBit) {
						drawHorizontalSegment(context, lastPoint.x, lastPoint.y, effNextX, nextY)
					} else {
						multiBitLabel.text = entry.signal.hexString
						multiBitLabel.horizontalAlignment = HorizontalAlignment.CENTER
						multiBitLabel.location =
							Point2D(effNextX + (lastPoint.x - effNextX) / 2, baseLineY - signalHeight / 2)
						drawMultiBitSegment(context, xR = lastPoint.x, xL = effNextX, first = false)
					}
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

		context.g.setClipBounds(clipBuffer)
	}

	private fun drawMultiBitRightBorder(context: DrawContext, xL: Double) {
		if (xL <= rightBorder - START_SIZE - MULTI_BIT_INSET) {
			drawMultiBitSegment(context, xR = rightBorder, xL = xL, first = true)
		}

		context.g.fillOval(rightBorder - START_SIZE, baseLineY - signalHeight / 2 - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)

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
				.moveTo(xR, baseLineY - signalHeight)
		} else {
			path
				.moveTo(xR, baseLineY - signalHeight / 2)
				.lineTo(xR - MULTI_BIT_INSET, baseLineY - signalHeight)
		}

		if (xL > bounds.minX) {
			path
				.lineTo(xL + MULTI_BIT_INSET, baseLineY - signalHeight)
				.lineTo(xL, baseLineY - signalHeight / 2)
				.lineTo(xL + MULTI_BIT_INSET, baseLineY)
		} else {
			path
				.lineTo(xL, baseLineY - signalHeight)
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
				.lineTo(xR - MULTI_BIT_INSET, baseLineY)
				.lineTo(xR, baseLineY - signalHeight / 2)
		}

		return path
	}

	override fun signalY(entry: SignalHistoryEntry<DigitalSignal>): Double {
		return if (entry.signal.bitAt(0).isSet) {
			baseLineY - signalHeight
		} else {
			baseLineY
		}
	}
}