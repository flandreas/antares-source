package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimeline
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import kotlin.math.max

class DigitalSignalHistoryDrawer : AbstractRectangle(Rectangle2D()), SignalHistoryDrawer {

	companion object {

		/** The height of an individual row.*/
		const val ROW_HEIGHT: Int = 40

		/** The maximum height the signal, i.e. the vertical distance in model coordinates between 0 and 1 signals.*/
		private const val SIGNAL_HEIGHT = 20.0

		/** The color used for drawing the background.*/
		private val BACKGROUND_COLOR get() = Themes.get<AntaresTheme>().screen

		/** The half size of the dot that marks the start of the signal curve, and therefore the current time.*/
		private const val START_SIZE = 2.0

		/** The horizontal inset used when drawing the arrow head of a multi-bit signal curve.*/
		private const val MULTIBIT_INSET = 3.0

		private const val BUFFER_END_CIRCLE_COUNT = 3
		private const val BUFFER_END_CIRCLE_RADIUS = 2.0
		private const val BUFFER_END_CIRCLE_DIST = 6.0
		private const val BUFFER_END_WIDTH = BUFFER_END_CIRCLE_COUNT * BUFFER_END_CIRCLE_DIST + 2 * BUFFER_END_CIRCLE_RADIUS

		/** The name of the [Boolean] property in [Properties] that determines whether signal curves are filled.*/
		const val PROP_FILL_SIGNAL = "DigitalSignalHistoryDrawer.fillSignal"
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

	private val fillSignal: Boolean get() = BaseModule.properties.getBoolean(PROP_FILL_SIGNAL)

	/** ---- [RectangularDrawable] interface*/

	override val lineWidth: Double get() = 0.0

	override fun draw(context: DrawContext) {

		// Draw background
		context.g.color = BACKGROUND_COLOR.backgroundColor
		context.g.fill(bounds)
		context.g.draw(bounds)

		// Draw horizontal axis
		context.g.color = BACKGROUND_COLOR.foregroundColor
		context.g.drawLine(rightBorder, baseLineY, bounds.minX, baseLineY)

		if (signalHistory == null || timeline == null || color == null) {
			return
		}

		if (gridSignalHistory != null) {
			// Draw vertical grid lines
			for (entry in gridSignalHistory!!.getReverseEntriesUntil(0)) {
				val x = max(rightBorder - timeline!!.getX(entry.time), bounds.minX)
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

	private val baseLineY: Double get() = bounds.maxY - 2

	private fun drawCurve(context: DrawContext) {
		val singleBit = signalHistory!!.last().signal.getBitWidth().width == 1
		var lastPoint = Point2D.ZERO
		var lastEntry: SignalHistoryEntry<DigitalSignal>? = null
		var effNextX: Double = rightBorder
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
					multiBitLabel.text = entry.signal.toHexString()
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
					multiBitLabel.text = entry.signal.toHexString()
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

	private fun drawBufferEnd(context: DrawContext, xL: Double) {
		val y = baseLineY - SIGNAL_HEIGHT / 2
		context.g.color = BACKGROUND_COLOR.foregroundColor

		var x = xL
		for (i in 1..BUFFER_END_CIRCLE_COUNT) {
			context.g.fillCircle(x, y, BUFFER_END_CIRCLE_RADIUS)
			x -= BUFFER_END_CIRCLE_DIST
		}
	}

	private fun drawSingleBitRightBorder(context: DrawContext, xL: Double, y: Double) {
		if (fillSignal) {
			context.g.color = color!!.backgroundColor
			context.g.fillRect(xL, y, rightBorder - xL, baseLineY - y)
		}
		context.g.color = color!!.foregroundColor
		context.g.drawLine(rightBorder, y, xL, y)
		context.g.fillOval(rightBorder - START_SIZE, y - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)
	}

	private fun drawMultiBitRightBorder(context: DrawContext, xL: Double) {
		if (xL <= rightBorder - START_SIZE - MULTIBIT_INSET) {
			drawMultiBitSegment(context, xR = rightBorder, xL = xL, first = true)
		}

		context.g.fillOval(rightBorder - START_SIZE, baseLineY - SIGNAL_HEIGHT / 2 - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)

		multiBitLabel.color = if (fillSignal) color!!.textColor else Themes.get<GraphTheme>().annotation.color.textColor
		multiBitLabel.draw(context)
	}

	private fun drawSingleBitSegment(context: DrawContext, xR: Double, yR: Double, xL: Double, yL: Double) {
		if (fillSignal) {
			context.g.color = color!!.backgroundColor
			context.g.fillRect(xL, yL, xR - xL, baseLineY - yL)
		}
		context.g.color = color!!.foregroundColor
		context.g.drawLine(xR, yR, xR, yL)
		context.g.drawLine(xR, yL, xL, yL)
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

	private fun signalY(entry: SignalHistoryEntry<DigitalSignal>): Double {
		return if (entry.signal.bitAt(0).isSet) {
			baseLineY - SIGNAL_HEIGHT
		} else {
			baseLineY
		}
	}
}