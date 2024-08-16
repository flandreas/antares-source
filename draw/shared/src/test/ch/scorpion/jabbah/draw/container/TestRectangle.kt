package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle

internal class TestRectangle(shape: RectangularShape) : AbstractRectangle(shape) {
	var mouseMoved = false
	var mousePressed = false
	private val handler = Handler()

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> = handler
	override fun draw(context: DrawContext) {}
	override val lineWidth: Double get() = 0.0
	override fun getTooltip(x: Double, y: Double, editable: Boolean): Tooltip = Tooltip("Test", x, y)

	private inner class Handler : InputEventHandlerAdapter<InputEventContext>() {
		override fun mouseMoved(context: InputEventContext): InputEventHandler<InputEventContext>? {
			if (this@TestRectangle.contains(context.x, context.y)) {
				mouseMoved = true
				return this
			}
			return null
		}

		override fun mousePressed(context: InputEventContext): InputEventHandler<InputEventContext>? {
			if (this@TestRectangle.contains(context.x, context.y)) {
				mousePressed = true
				return this
			}
			return null
		}
	}
}