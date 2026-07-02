package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.LinearColorGradient
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistory
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

abstract class AbstractSignalHistoryDrawer<T: Any>(
	protected val rightInset: Int,
	private val background: CompositeColor,
	protected val yAxis: SignalHistoryYAxis<T>?
): AbstractRectangle(Rectangle2D()), SignalHistoryDrawer<T> {

	companion object {

		/** The name of the [Boolean] property in [Properties] that determines whether signal curves are filled.*/
		const val PROP_FILL_SIGNAL = "DigitalSignalHistoryDrawer.fillSignal"

		/** The half size of the dot that marks the start of the signal curve, and therefore the current time.*/
		const val START_SIZE = 2.0

		private val BASELINE_STROKE = Stroke(1f)
		val CURVE_STROKE = Stroke(1f)
		private val GRID_LINE_STROKE = Stroke(0.5f)

		private const val BUFFER_END_CIRCLE_COUNT = 3
		private const val BUFFER_END_CIRCLE_RADIUS = 2.0
		private const val BUFFER_END_CIRCLE_DIST = 6.0
		const val BUFFER_END_WIDTH = BUFFER_END_CIRCLE_COUNT * BUFFER_END_CIRCLE_DIST + 2 * BUFFER_END_CIRCLE_RADIUS

		private const val OFFSET_INDICATOR_WIDTH = 20
	}

	/** The [SignalHistory] drawn by this [AbstractSignalHistoryDrawer].*/
	protected var signalHistory: SignalHistory<T>? = null
		private set

	/** The [SignalHistory] whose signal times determine the locations of the vertical grid lines.*/
	private var gridSignalHistory: SignalHistory<T>? = null

	/** The [SignalHistoryTimeline] used for mapping signal time to horizontal model coordinates.*/
	protected var timeline: SignalHistoryTimeline? = null
		private set

	/** The color to be used for drawing the signal curve.*/
	protected var color: CompositeColor? = null
		private set

	protected val fillSignal: Boolean get() = BaseModule.properties.getBoolean(PROP_FILL_SIGNAL)

	/** Used for painting an indicator if the drawer is offset, i.e. [scrollX] is not zero. */
	private val offsetIndicatorGradient = LinearColorGradient(
		Point2D.ZERO,
		background.backgroundColor.withAlpha(0),
		Point2D(OFFSET_INDICATOR_WIDTH, 0),
		background.backgroundColor.darker()
	)

	/** ---- [SignalHistoryDrawer] interface */

	override var scrollX: Double = 0.0

	override fun bind(
		signalHistory: SignalHistory<T>?,
		gridSignalHistory: SignalHistory<T>?,
		timeline: SignalHistoryTimeline?,
		color: CompositeColor
	) {
		this.signalHistory = signalHistory
		this.gridSignalHistory = gridSignalHistory
		this.timeline = timeline
		this.color = color
	}

	/** ---- [RectangularDrawable] interface*/

	override val lineWidth: Double get() = 0.0

	override fun draw(context: DrawContext) {
		drawBackground(context)
		drawBaseline(context)

		if (signalHistory == null || timeline == null || color == null) {
			return
		}

		if (!signalHistory!!.isEmpty) {
			if (gridEnabled) {
				drawGrid(context)
			}
			drawCurve(context)
		}

		if (scrollX > 0) {
			drawOffsetIndicator(context)
		}
	}

	protected val rightBorder: Double get() = bounds.maxX - rightInset

	protected val baseLineY: Double get() = yAxis?.baselineY ?: (bounds.maxY - 2)

	/** ---- [AbstractSignalHistoryDrawer]*/

	protected abstract val gridEnabled: Boolean

	/**
	 * The maximum height of the signal, i.e. the vertical distance in model coordinates
	 * between min and max signals.
	 */
	protected abstract val signalHeight: Double

	// Visible for testing
	abstract fun drawCurve(context: DrawContext)

	protected abstract fun signalY(entry: SignalHistoryEntry<T>): Double

	private fun drawBackground(context: DrawContext) {
		context.g.color = background.backgroundColor
		context.g.fill(bounds)
		context.g.draw(bounds)
	}

	private fun drawBaseline(context: DrawContext) {
		context.g.color = background.foregroundColor
		context.g.stroke = BASELINE_STROKE
		context.g.drawLine(rightBorder, baseLineY, bounds.minX, baseLineY)
	}

	private fun drawGrid(context: DrawContext) {
		if (gridSignalHistory != null) {
			context.g.stroke = GRID_LINE_STROKE
			for (entry in gridSignalHistory!!.getReverseEntriesUntil(0)) {
				val x = max(rightBorder - timeline!!.getX(entry.time) + scrollX, bounds.minX)
				if (x <= bounds.minX) {
					break
				}
				if (x < rightBorder ) {
					context.g.drawLine(x, bounds.minY, x, bounds.maxY)
				}
			}
		}
	}

	protected fun drawRightBorder(context: DrawContext, xL: Double, y: Double) {
		if (xL != rightBorder) {
			if (fillSignal) {
				context.g.color = color!!.backgroundColor
				context.g.fillRect(xL, min(y, baseLineY), rightBorder - xL, abs(baseLineY - y))
			}
			context.g.color = color!!.foregroundColor
			context.g.drawLine(rightBorder, y, xL, y)
		}
		context.g.fillOval(rightBorder - START_SIZE, y - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)
	}

	protected fun drawHorizontalSegment(context: DrawContext, xR: Double, yR: Double, xL: Double, yL: Double) {
		if (fillSignal) {
			context.g.color = color!!.backgroundColor
			context.g.fillRect(xL, min(yL, baseLineY), xR - xL, abs(baseLineY - yL))
		}
		context.g.color = color!!.foregroundColor
		context.g.drawLine(xR, yR, xR, yL)
		context.g.drawLine(xR, yL, xL, yL)
	}

	private val nonHorSegX = IntArray(4) { 0 }
	private val nonHorSegY = IntArray(4) { 0 }

	protected fun drawNonHorizontalSegment(context: DrawContext, xR: Double, yR: Double, xL: Double, yL: Double) {
		nonHorSegX[0] = xL.toInt()
		nonHorSegX[1] = xL.toInt()
		nonHorSegX[2] = xR.toInt()
		nonHorSegX[3] = xR.toInt()
		nonHorSegY[0] = baseLineY.toInt()
		nonHorSegY[1] = yL.toInt()
		nonHorSegY[2] = yR.toInt()
		nonHorSegY[3] = baseLineY.toInt()

		if (fillSignal) {
			context.g.color = color!!.backgroundColor
			context.g.fillPolygon(nonHorSegX, nonHorSegY, 4)
		}
		context.g.color = color!!.foregroundColor
		context.g.drawLine(xR, yR, xL, yL)
	}

	protected fun drawBufferEnd(context: DrawContext, xL: Double) {
		val y = baseLineY
		context.g.color = background.foregroundColor

		var x = xL
		for (i in 1..BUFFER_END_CIRCLE_COUNT) {
			context.g.fillCircle(x, y, BUFFER_END_CIRCLE_RADIUS)
			x -= BUFFER_END_CIRCLE_DIST
		}
	}

	private fun drawOffsetIndicator(context: DrawContext) {
		context.g.paint = offsetIndicatorGradient

		// Use translated context so that offsetIndicatorGradient can be express in relative coordinates
		context.translated(bounds.maxX - rightInset - OFFSET_INDICATOR_WIDTH + START_SIZE, bounds.minY) {
			it.g.fillRect(0, 0, OFFSET_INDICATOR_WIDTH, bounds.heightInt)
		}
	}
}