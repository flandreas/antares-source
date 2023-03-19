package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.CompositeColor

interface SignalHistoryYAxis<T: Any> : RectangularDrawable {
	fun signalY(signal: T): Double
	val baselineY: Double
}

abstract class AbstractSignalHistoryYAxis<T: Any>(
	bounds: RectangularShape,
	protected val color: CompositeColor
) : AbstractRectangle(bounds), SignalHistoryYAxis<T> {

	companion object {
		private const val TOP_INSET = 6
		private const val BOTTOM_INSET = 2
	}

	override val baselineY: Double get() = bounds.maxY - BOTTOM_INSET

	override fun draw(context: DrawContext) {
		drawYAxis(context)
		drawScale(context)
	}

	protected abstract fun drawScale(context: DrawContext)

	private fun drawYAxis(context: DrawContext) {
		context.g.color = color.foregroundColor
		context.g.drawLine(bounds.minX, bounds.minY + TOP_INSET, bounds.minX, baselineY)
	}
}