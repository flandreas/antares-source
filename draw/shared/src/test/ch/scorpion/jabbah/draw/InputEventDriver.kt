package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.MouseEventImpl
import ch.scorpion.jabbah.base.event.MouseEventType

/**
 * Provides a convenient API for composing events and forwarding them to an [InputEventHandler].
 * This implementation remembers the [InputEventHandler] returned from the last called handler method,
 * and uses it again for the next call, otherwise uses the [InputEventHandler] of the [DrawableContainer].
 * This is how real clients of the event system are supposed to work.
 */
class InputEventDriver(
	private val view: View<InputEventContext>,
	private val container: DrawableContainer<Drawable>
) {

	private var handler: InputEventHandler<InputEventContext>? = null

	fun moveMouseTo(x: Int, y: Int, modifiers: Int = 0): InputEventHandler<InputEventContext>? {
		val context = context(MouseEventType.MOVED, x, y, modifiers)
		return keepHandler(chooseHandler(context).mouseMoved(context))
	}

	private fun context(type: MouseEventType, x: Int, y: Int, modifiers: Int = 0, clickCount: Int = 1): InputEventContext {
		return InputEventContext(
			view = view,
			mouseEvent = MouseEventImpl(type, x = x, y = y, modifiers = modifiers, clickCount = clickCount),
			x = x.toDouble(),
			y = y.toDouble())
	}

	private fun chooseHandler(context: InputEventContext): InputEventHandler<InputEventContext> {
		return handler ?: container.getInputEventHandler(context)
	}

	private fun keepHandler(handler: InputEventHandler<InputEventContext>?): InputEventHandler<InputEventContext>? {
		this.handler = handler
		return handler
	}
}