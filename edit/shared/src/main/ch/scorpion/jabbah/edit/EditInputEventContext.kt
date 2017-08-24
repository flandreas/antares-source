package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.InputEventContext

/**
 * Represents an [InputEventContext] on the edit level.
 */
class EditInputEventContext(
    val editor: Editor,
    mouseEvent: MouseEvent? = null,
    keyEvent: KeyEvent? = null,
    x: Double = 0.0,
    y: Double = 0.0
) : InputEventContext(editor.view, mouseEvent, keyEvent, x, y) {

    /** Returns a copy of this [EditInputEventContext] with other x and y coordinates*/
    override fun withXY(x: Double, y: Double): EditInputEventContext {
        return EditInputEventContext(
            editor = this.editor,
            mouseEvent = this.mouseEvent,
            keyEvent = this.keyEvent,
            x = x,
            y = y
        )
    }

    fun drawingView(): DrawingView<Drawing<Component>> = editor.view
}