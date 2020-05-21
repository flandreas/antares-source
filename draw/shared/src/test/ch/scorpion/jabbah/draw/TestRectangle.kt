package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.draw.drawable.AbstractRectangle

class TestRectangle(x: Int = 0, y: Int = 0, w: Int = 0, h: Int = 0) : AbstractRectangle(x, y, w, h) {

	override fun draw(context: DrawContext) { }

	override val lineWidth: Double get() = 1.0
}