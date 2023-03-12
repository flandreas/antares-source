package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimeline
import kotlin.math.max

abstract class AbstractAntaresSignalHistoryDrawer<T: Any>
	: AbstractRectangle(Rectangle2D()), SignalHistoryDrawer<T> {

	companion object {

		/** The maximum height the signal, i.e. the vertical distance in model coordinates between 0 and 1 signals.*/
		const val SIGNAL_HEIGHT = 20.0

		/** The name of the [Boolean] property in [Properties] that determines whether signal curves are filled.*/
		const val PROP_FILL_SIGNAL = "DigitalSignalHistoryDrawer.fillSignal"

		/** The color used for drawing the background.*/
		val BACKGROUND_COLOR get() = Themes.get<AntaresTheme>().screen

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

	/** The [SignalHistory] drawn by this [AbstractAntaresSignalHistoryDrawer].*/
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
		// Draw background
		context.g.color = BACKGROUND_COLOR.backgroundColor
		context.g.fill(bounds)
		context.g.draw(bounds)

		// Draw horizontal axis
		context.g.color = BACKGROUND_COLOR.foregroundColor
		context.g.stroke = BASELINE_STROKE
		context.g.drawLine(rightBorder, baseLineY, bounds.minX, baseLineY)

		if (signalHistory == null || timeline == null || color == null) {
			return
		}

		if (gridSignalHistory != null) {
			// Draw vertical grid lines
			context.g.stroke = GRID_LINE_STROKE
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

	protected val rightBorder: Double get() = bounds.maxX - 20

	protected val baseLineY: Double get() = bounds.maxY - 2


	/** ---- [AbstractAntaresSignalHistoryDrawer]*/

	protected abstract fun drawCurve(context: DrawContext)

	protected abstract fun signalY(entry: SignalHistoryEntry<T>): Double

	protected fun drawSingleBitRightBorder(context: DrawContext, xL: Double, y: Double) {
		if (fillSignal) {
			context.g.color = color!!.backgroundColor
			context.g.fillRect(xL, y, rightBorder - xL, baseLineY - y)
		}
		context.g.color = color!!.foregroundColor
		context.g.drawLine(rightBorder, y, xL, y)
		context.g.fillOval(rightBorder - START_SIZE, y - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)
	}

	protected fun drawSingleBitSegment(context: DrawContext, xR: Double, yR: Double, xL: Double, yL: Double) {
		if (fillSignal) {
			context.g.color = color!!.backgroundColor
			context.g.fillRect(xL, yL, xR - xL, baseLineY - yL)
			context.g.drawRect(xL, yL, xR - xL, baseLineY - yL)
		}
		context.g.color = color!!.foregroundColor
		context.g.drawLine(xR, yR, xR, yL)
		context.g.drawLine(xR, yL, xL, yL)
	}

	protected fun drawBufferEnd(context: DrawContext, xL: Double) {
		val y = baseLineY - SIGNAL_HEIGHT / 2
		context.g.color = BACKGROUND_COLOR.foregroundColor

		var x = xL
		for (i in 1..BUFFER_END_CIRCLE_COUNT) {
			context.g.fillCircle(x, y, BUFFER_END_CIRCLE_RADIUS)
			x -= BUFFER_END_CIRCLE_DIST
		}
	}
}