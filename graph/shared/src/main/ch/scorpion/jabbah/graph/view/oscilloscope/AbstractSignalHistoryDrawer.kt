package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import kotlin.math.max

abstract class AbstractSignalHistoryDrawer<T: Any>(
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

	/** ---- [SignalHistoryDrawer] interface */

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
			drawGrid(context)
			drawCurve(context)
		}
	}

	protected val rightBorder: Double get() = bounds.maxX - 20

	protected val baseLineY: Double get() = yAxis?.baselineY ?: bounds.maxY - 2

	/** ---- [AbstractSignalHistoryDrawer]*/

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
				val x = max(rightBorder - timeline!!.getX(entry.time), bounds.minX)
				if (x <= bounds.minX) {
					break
				}
				context.g.drawLine(x, bounds.minY, x, bounds.maxY)
			}
		}
	}

	protected fun drawRightBorder(context: DrawContext, xL: Double, y: Double) {
		if (xL != rightBorder) {
			if (fillSignal) {
				context.g.color = color!!.backgroundColor
				context.g.fillRect(xL, y, rightBorder - xL, baseLineY - y)
			}
			context.g.color = color!!.foregroundColor
			context.g.drawLine(rightBorder, y, xL, y)
		}
		context.g.fillOval(rightBorder - START_SIZE, y - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)
	}

	protected fun drawHorizontalSegment(context: DrawContext, xR: Double, yR: Double, xL: Double, yL: Double) {
		if (fillSignal) {
			context.g.color = color!!.backgroundColor
			context.g.fillRect(xL, yL, xR - xL, baseLineY - yL)
			context.g.drawRect(xL, yL, xR - xL, baseLineY - yL)
		}
		context.g.color = color!!.foregroundColor
		context.g.drawLine(xR, yR, xR, yL)
		context.g.drawLine(xR, yL, xL, yL)
	}

	/*
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
	 */

	protected fun drawBufferEnd(context: DrawContext, xL: Double) {
		val y = baseLineY
		context.g.color = background.foregroundColor

		var x = xL
		for (i in 1..BUFFER_END_CIRCLE_COUNT) {
			context.g.fillCircle(x, y, BUFFER_END_CIRCLE_RADIUS)
			x -= BUFFER_END_CIRCLE_DIST
		}
	}
}