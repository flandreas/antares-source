package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * An API for driving an [Editor]'s current [Tool] during tests.
 */
class EditorToolDriver(
	private val editor: Editor
) {

	fun mouseMoveTo(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		editor.currentTool.mouseMoved(event(MouseEventType.MOVED, x, y, modifiers), Point2D(x, y))
		return this
	}

	fun pressMouseAt(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		editor.currentTool.mousePressed(event(MouseEventType.PRESSED, x, y, modifiers), Point2D(x, y))
		return this
	}

	fun dragMouseTo(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		editor.currentTool.mouseDragged(event(MouseEventType.DRAGGED, x, y, modifiers), Point2D(x, y))
		return this
	}

	fun releaseMouseAt(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		editor.currentTool.mouseReleased(event(MouseEventType.RELEASED, x, y, modifiers), Point2D(x, y))
		return this
	}

	fun pressKey(keyCode: Int): EditorToolDriver {
		editor.currentTool.keyPressed(event(KeyEventType.PRESSED, keyCode))
		return this
	}

	fun releaseKey(keyCode: Int): EditorToolDriver {
		editor.currentTool.keyReleased(event(KeyEventType.RELEASED, keyCode))
		return this
	}

	private fun event(type: MouseEventType, x: Int, y: Int, modifiers: Int = 0): MouseEvent =
		MouseEventImpl(type, x, y, button = Button.BUTTON1, modifiers = modifiers)

	private fun event(type: KeyEventType, keyCode: Int): KeyEvent =
		KeyEventImpl(type, key = keyCode, keyChar = ' ')
}