package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimeline
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.view.style.GraphTheme

class DigitalSignalHistoryDrawer : AbstractRectangle(Rectangle2D()), SignalHistoryDrawer {

	companion object {

		/** The height of an individual row to be used in [OscilloscopeView].*/
		const val ROW_HEIGHT: Int = 40

		private val LOG by logger(DigitalSignalHistoryDrawer::class)

		/** The maximum height the signal, i.e. the vertical distance in model coordinates between 0 and 1 signals.*/
		private const val SIGNAL_HEIGHT = 20.0

		/** The color used for drawing the background.*/
		private val BACKGROUND_COLOR = Color.BLACK

		/** The color used for drawing the horizontal axis (and the vertical grid lines).*/
		private val AXIS_COLOR = Color(64, 64, 64)

		/** The half size of the dot that marks the start of the signal curve, and therefore the current time.*/
		private const val START_SIZE = 2.0

		/** The horizontal inset used when drawing the arrow head of a multi-bit signal curve.*/
		private const val MULTIBIT_INSET = 3.0

		/** Determines whether signal curves are filled.*/
		private const val FILL_SIGNAL = false
	}

	/** The [SignalHistory] drawn by this [DigitalSignalHistoryDrawer].*/
	private var signalHistory: SignalHistory<DigitalSignal>? = null

	/** The [SignalHistory] whose signal times determine the locations of the vertical grid lines.*/
	private var gridSignalHistory: SignalHistory<DigitalSignal>? = null

	/** The [SignalHistoryTimeline] used for mapping signal time to horizontal model coordinates.*/
	private var timeline: SignalHistoryTimeline? = null

	/** The color to be used for drawing the signal curve.*/
	private var color: CompositeColor? = null

	/** Uses for drawing the signal value of a multi-bit [DigitalSignal].*/
	private val multiBitLabel = Label(
		text = "",
		font = Themes.get<GraphTheme>().annotation.font,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER
	)

	/** ---- [RectangularDrawable] interface*/

	override val lineWidth: Double get() = 0.0

	override fun draw(context: DrawContext) {

		// Draw background
		context.g.color = BACKGROUND_COLOR
		context.g.fill(bounds)
		context.g.draw(bounds)

		// Draw horizontal axis
		context.g.color = AXIS_COLOR
		context.g.drawLine(rightBorder, baseLineY, bounds.minX, baseLineY)

		if (signalHistory == null || timeline == null || color == null) {
			return
		}

		if (gridSignalHistory != null) {
			// Draw vertical grid lines
			for (entry in gridSignalHistory!!.getReverseEntriesUntil(0)) {
				val x = Math.max(rightBorder - timeline!!.getX(entry.time), bounds.minX)
				if (x <= bounds.minX) {
					break
				}
				context.g.drawLine(x, bounds.minY, x, bounds.maxY)
			}
		}

		if (signalHistory!!.size == 0) {
			return
		}

		drawCurve(context)
	}

	/** ---- [SignalHistoryDrawer] interface */

	override fun bind(
		signalHistory: SignalHistory<Any>?,
		gridSignalHistory: SignalHistory<Any>?,
		timeline: SignalHistoryTimeline?,
		color: CompositeColor
	) {
		this.signalHistory = signalHistory as SignalHistory<DigitalSignal>?
		this.gridSignalHistory = gridSignalHistory as SignalHistory<DigitalSignal>?
		this.timeline = timeline
		this.color = color
	}

	/** ---- [DigitalSignalHistoryDrawer] */

	private val rightBorder: Double get() = bounds.maxX - 20

	private val baseLineY: Double get() = bounds.maxY - 10

	private fun drawCurve(context: DrawContext) {
		val singleBit = signalHistory!!.last().signal.getBitWidth().width == 1
		var lastPoint = Point2D.ZERO
		var lastEntry: SignalHistoryEntry<DigitalSignal>? = null
		for (entry in signalHistory!!.getReverseEntriesUntil(0)) {
			val x = rightBorder - timeline!!.getX(entry.time)
			val y = signalY(entry)
			if (lastEntry == null) {
				// Right border
				lastPoint = Point2D(x, y)
				val effNextX = Math.max(x, bounds.minX)

				if (singleBit) {
					drawSingleBitRightBorder(context, effNextX, y)
				} else {
					drawMultiBitRightBorder(context, effNextX)
					// Draw signal value
					multiBitLabel.text = entry.signal.toHexString()
					multiBitLabel.horizontalAlignment = HorizontalAlignment.LEFT
					multiBitLabel.location = Point2D(effNextX + MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT / 2)
					multiBitLabel.draw(context)
				}

				if (x <= bounds.minX) {
					break
				}
			} else {
				val nextX = x
				val nextY = y
				val effNextX = Math.max(nextX, bounds.minX)

				if (singleBit) {
					drawSingleBitSegment(context, lastPoint.x, lastPoint.y, effNextX, nextY)
				} else {
					drawMultiBitSegment(context, xR = lastPoint.x, xL = effNextX)
					// Draw signal value
					multiBitLabel.text = entry.signal.toHexString()
					multiBitLabel.horizontalAlignment = HorizontalAlignment.CENTER
					multiBitLabel.location = Point2D(effNextX + (lastPoint.x - effNextX) / 2, baseLineY - SIGNAL_HEIGHT / 2)
					multiBitLabel.draw(context)
				}

				if (nextX <= bounds.minX) {
					break
				}

				lastPoint = Point2D(effNextX, nextY)
			}

			lastEntry = entry
		}
	}

	private fun drawSingleBitRightBorder(context: DrawContext, xL: Double, y: Double) {
		if (FILL_SIGNAL) {
			context.g.color = color!!.foregroundColor
			context.g.fillRect(xL, y, rightBorder - xL, baseLineY - y)
			context.g.color = color!!.backgroundColor
		} else {
			context.g.color = color!!.foregroundColor
		}
		context.g.drawLine(rightBorder, y, xL, y)
		context.g.fillOval(rightBorder - START_SIZE, y - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)
	}

	private fun drawMultiBitRightBorder(context: DrawContext, xL: Double) {
		context.g.color = color!!.foregroundColor
		if (xL <= rightBorder - START_SIZE - MULTIBIT_INSET) {
			// Upper and lower line
			context.g.drawLine(rightBorder, baseLineY - SIGNAL_HEIGHT, xL + MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT)
			context.g.drawLine(rightBorder, baseLineY, xL + MULTIBIT_INSET, baseLineY)

			// Left arrow head
			context.g.drawLine(xL + MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT, xL, baseLineY - SIGNAL_HEIGHT / 2)
			context.g.drawLine(xL + MULTIBIT_INSET, baseLineY, xL, baseLineY - SIGNAL_HEIGHT / 2)
		}

		context.g.fillOval(rightBorder - START_SIZE, baseLineY - SIGNAL_HEIGHT / 2 - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)
	}

	private fun drawSingleBitSegment(context: DrawContext, xR: Double, yR: Double, xL: Double, yL: Double) {
		if (FILL_SIGNAL) {
			context.g.color = color!!.foregroundColor
			context.g.fillRect(xL, yL, xR - xL, baseLineY - yL)
			context.g.color = color!!.backgroundColor
		} else {
			context.g.color = color!!.foregroundColor
		}
		context.g.drawLine(xR, yR, xR, yL)
		context.g.drawLine(xR, yL, xL, yL)
	}

	private fun drawMultiBitSegment(context: DrawContext, xR: Double, xL: Double) {
		context.g.color = color!!.foregroundColor

		// Right arrow head
		context.g.drawLine(xR, baseLineY - SIGNAL_HEIGHT / 2, xR - MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT)
		context.g.drawLine(xR, baseLineY - SIGNAL_HEIGHT / 2, xR - MULTIBIT_INSET, baseLineY)

		// Upper and lower line
		context.g.drawLine(xR - MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT, xL + MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT)
		context.g.drawLine(xR - MULTIBIT_INSET, baseLineY, xL + MULTIBIT_INSET, baseLineY)

		// Left arrow head
		context.g.drawLine(xL + MULTIBIT_INSET, baseLineY - SIGNAL_HEIGHT, xL, baseLineY - SIGNAL_HEIGHT / 2)
		context.g.drawLine(xL + MULTIBIT_INSET, baseLineY, xL, baseLineY - SIGNAL_HEIGHT / 2)
	}

	private fun signalY(entry: SignalHistoryEntry<DigitalSignal>): Double {
		return if (entry.signal.bitAt(0).isSet) {
			baseLineY - SIGNAL_HEIGHT
		} else {
			baseLineY
		}
	}
}