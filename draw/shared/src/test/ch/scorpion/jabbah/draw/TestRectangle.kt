package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.draw.drawable.AbstractRectangle

class TestRectangle(x: Int, y: Int, w: Int, h: Int) : AbstractRectangle(x, y, w, h) {

	override fun draw(context: DrawContext) { }

	override val lineWidth: Double get() = 1.0
}