package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor

/**
 * A wrapper around [InputEventHandler] that provides a convenient interface for creating events for
 * the wrapped [InputEventHandler]. Mainly used for testing purposes, but potentially useful for other applications.
 */
open class InputEventDriver(
	protected open val editor: Editor,
	protected val handler: InputEventHandler<EditInputEventContext>
) {

	protected open fun mouseMoveTo(x: Int, y: Int, modifiers: Int = 0): InputEventDriver {
		handler.mouseMoved(context(MouseEventType.MOVED, x, y, modifiers))
		return this
	}

	protected fun pressMouseAt(x: Int, y: Int, modifiers: Int = 0): InputEventDriver {
		handler.mousePressed(context(MouseEventType.PRESSED, x, y, modifiers))
		return this
	}

	protected fun clickMouseAt(x: Int, y: Int, modifiers: Int = 0): InputEventDriver {
		handler.mouseClicked(context(MouseEventType.CLICKED, x, y, modifiers))
		return this
	}

	protected fun doubleClickMouseAt(x: Int, y: Int, modifiers: Int = 0): InputEventDriver {
		handler.mouseClicked(context(MouseEventType.CLICKED, x, y, modifiers, clickCount = 2))
		return this
	}

	protected fun dragMouseTo(x: Int, y: Int): InputEventDriver {
		handler.mouseDragged(context(MouseEventType.DRAGGED, x, y))
		return this
	}

	protected fun releaseMouseAt(x: Int, y: Int): InputEventDriver {
		handler.mouseReleased(context(MouseEventType.RELEASED, x, y))
		return this
	}

	protected fun pressKey(keyCode: Int): InputEventDriver {
		handler.keyPressed(context(KeyEventType.PRESSED, keyCode))
		return this
	}

	protected fun pressEscape(): InputEventDriver {
		pressKey(KeyEvent.VK_ESCAPE)
		return this
	}

	protected fun pressAlt(): InputEventDriver {
		pressKey(KeyEvent.VK_ALT)
		return this
	}

	protected fun context(type: MouseEventType, x: Int, y: Int, modifiers: Int = 0, clickCount: Int = 1): EditInputEventContext {
		return EditInputEventContext(
			editor = editor,
			mouseEvent = MouseEventImpl(type, x = x, y = y, modifiers = modifiers, clickCount = clickCount),
			x = x.toDouble(),
			y = y.toDouble())
	}

	protected fun context(type: KeyEventType, keyCode: Int): EditInputEventContext {
		return EditInputEventContext(editor, keyEvent = KeyEventImpl(type, key = keyCode, keyChar = ' '))
	}
}