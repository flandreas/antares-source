package ch.scorpion.jabbah.graph.view.connect.highlight

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * Draws a [ConnectionPointHighlight] as a circle.
 */
class ConnectionPointHighlightCircle : AbstractRectangularUnzoomable(ConnectionPointHighlight.Companion.SIZE_HALF), ConnectionPointHighlight {

	companion object {

		private const val INSET = 3

		private val STROKE = Stroke(DrawStyleModule.styleProvider.getStyle(GraphStyleType.Companion.EDGE).stroke.width)

		fun drawNormalViewAt(location: Point2D, context: DrawContext) {
			context.g.color = DrawModule.properties.getColor(ConnectionPointHighlight.Companion.PROP_COLOR)
			context.g.fillCircle(location.x, location.y, ConnectionPointHighlight.Companion.SIZE_HALF - INSET)
			context.g.stroke = STROKE
			context.g.drawCircle(location.x, location.y, ConnectionPointHighlight.Companion.SIZE_HALF)
		}
	}

	override val lineWidth: Double get() = STROKE.width.toDouble()

	override var alternativeView: Boolean = false
		set(value) {
			if (value != alternativeView) {
				invalidate()
				field = value
				invalidate()
				validate()
			}
		}

	override fun draw(context: DrawContext) {
		context.g.color = DrawModule.properties.getColor(ConnectionPointHighlight.Companion.PROP_COLOR)
		if (alternativeView) {
			drawAlternativeView(context)
		} else {
			drawNormalView(context)
		}
	}

	private fun drawNormalView(context: DrawContext) {
		drawNormalViewAt(getViewRectangle().center, context)
	}

	private fun drawAlternativeView(context: DrawContext) {
		val rect = getViewRectangle()
		context.g.fillRect(rect.x.toInt() + INSET, rect.y.toInt() + INSET, rect.width.toInt() - 2*INSET, rect.height.toInt() - 2*INSET)
		context.g.stroke = STROKE
		context.g.drawRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
	}
}