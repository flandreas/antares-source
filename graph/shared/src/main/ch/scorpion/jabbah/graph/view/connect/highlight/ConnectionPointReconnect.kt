package ch.scorpion.jabbah.graph.view.connect.highlight

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

class ConnectionPointReconnect : AbstractRectangularUnzoomable(SIZE_HALF), ConnectionPointHighlight {

	companion object {
		private const val F = 3.0
		private const val SIZE_HALF = F * 6.0
		private const val INSET = F * 3.0
		private const val ARROW = F * 2.0
		private val stroke = Stroke(DrawStyleModule.styleProvider.getStyle(GraphStyleType.Companion.EDGE).stroke.width)

		private val NORTH = System.createPath()
			.moveTo(0.0, -SIZE_HALF)
			.lineTo(ARROW, -SIZE_HALF + ARROW)
			.lineTo(-ARROW, -SIZE_HALF + ARROW)
			.close()

		private val SOUTH = System.createPath()
			.moveTo(0.0, SIZE_HALF)
			.lineTo(ARROW, SIZE_HALF - ARROW)
			.lineTo(-ARROW, SIZE_HALF - ARROW)
			.close()

		private val EAST = System.createPath()
			.moveTo(SIZE_HALF, 0.0)
			.lineTo(SIZE_HALF - ARROW, -ARROW)
			.lineTo(SIZE_HALF - ARROW, ARROW)
			.close()

		private val WEST = System.createPath()
			.moveTo(-SIZE_HALF, 0.0)
			.lineTo(-SIZE_HALF + ARROW, -ARROW)
			.lineTo(-SIZE_HALF + ARROW, ARROW)
			.close()
	}

	override val lineWidth: Double get() = stroke.width.toDouble()
	override var alternativeView: Boolean = false

	override fun draw(context: DrawContext) {
		context.g.color = DrawModule.properties.getColor(ConnectionPointHighlight.Companion.PROP_COLOR)
		context.g.stroke = stroke

		val rect = getViewRectangle()
		context.g.fillOval(
			rect.x.toInt() + INSET, rect.y.toInt() + INSET,
			rect.width.toInt() - 2 * INSET, rect.height.toInt() - 2* INSET)

		context.translated(rect.center) {
			it.g.fill(NORTH)
			it.g.fill(SOUTH)
			it.g.fill(EAST)
			it.g.fill(WEST)
		}
	}
}