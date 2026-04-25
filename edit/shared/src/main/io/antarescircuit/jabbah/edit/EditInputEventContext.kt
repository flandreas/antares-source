package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.draw.InputEventContext

/**
 * Represents an [InputEventContext] on the edit level.
 */
class EditInputEventContext(
    val editor: Editor,
    mouseEvent: MouseEvent? = null,
    keyEvent: KeyEvent? = null,
    x: Double = 0.0,
    y: Double = 0.0,
    readonly: Boolean = false
) : InputEventContext(editor.view, mouseEvent, keyEvent, x, y, readonly) {

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

	val drawingView: DrawingView<Component, Drawing<Component>> get() = editor.view
}