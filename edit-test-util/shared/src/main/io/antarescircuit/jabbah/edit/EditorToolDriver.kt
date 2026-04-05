package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.event.KeyEventImpl
import io.antarescircuit.jabbah.base.event.KeyEventType
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.base.event.MouseEventImpl
import io.antarescircuit.jabbah.base.event.MouseEventType
import io.antarescircuit.jabbah.base.geom.Point2D

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

	fun moveMouseAndPressAt(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		mouseMoveTo(x, y, modifiers)
		pressMouseAt(x, y, modifiers)
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

	fun dragMouseAndReleaseAt(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		dragMouseTo(x, y, modifiers)
		releaseMouseAt(x, y, modifiers)
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

	fun pressAndDragTo(x1: Int, y1: Int, x2: Int, y2: Int, modifiers: Int = 0): EditorToolDriver {
		mouseMoveTo(x1, y1, modifiers)
		pressMouseAt(x1, y1, modifiers)
		dragMouseTo(x2, y2)
		releaseMouseAt(x2, y2)
		return this
	}

	private fun event(type: MouseEventType, x: Int, y: Int, modifiers: Int = 0): MouseEvent =
        MouseEventImpl(type, x, y, button = Button.BUTTON1, modifiers = modifiers)

	private fun event(type: KeyEventType, keyCode: Int): KeyEvent =
        KeyEventImpl(type, key = keyCode, keyChar = ' ')
}