package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.event.MouseEventImpl
import ch.scorpion.jabbah.base.event.MouseEventType
import ch.scorpion.jabbah.base.event.MouseEventType.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool

/**
 * An API for driving an [Editor]'s current [Tool] during tests.
 */
class EditorToolDriver(
	private val editor: Editor
) {

	fun mouseMoveTo(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		editor.currentTool.mouseMoved(event(MOVED, x, y, modifiers), Point2D(x, y))
		return this
	}

	fun pressMouseAt(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		editor.currentTool.mousePressed(event(PRESSED, x, y, modifiers), Point2D(x, y))
		return this
	}

	fun dragMouseTo(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		editor.currentTool.mouseDragged(event(DRAGGED, x, y, modifiers), Point2D(x, y))
		return this
	}

	fun releaseMouseAt(x: Int, y: Int, modifiers: Int = 0): EditorToolDriver {
		editor.currentTool.mouseReleased(event(RELEASED, x, y, modifiers), Point2D(x, y))
		return this
	}

	private fun event(type: MouseEventType, x: Int, y: Int, modifiers: Int = 0): MouseEvent {
		return MouseEventImpl(type, x, y, button = Button.BUTTON1, modifiers = modifiers)
	}
}