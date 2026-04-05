package io.antarescircuit.jabbah.draw.container

import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.geom.MutableRectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle

internal class TestRectangle(shape: MutableRectangularShape) : AbstractRectangle(shape) {
	var mouseMoved = false
	var mousePressed = false
	private val handler = Handler()

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> = handler
	override fun draw(context: DrawContext) {}
	override val lineWidth: Double get() = 0.0
	override fun <T: InputEventContext> getTooltip(context: T): Tooltip = Tooltip("Test", context.x, context.y)

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