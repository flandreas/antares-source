package io.antarescircuit.jabbah.edit.tool

import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Tool

abstract class AbstractTool(
    val editor: Editor
) : Tool {

    protected fun keyEventContext(e: KeyEvent): EditInputEventContext =
        EditInputEventContext(editor = editor, keyEvent = e)

    protected fun mouseEventContext(e: MouseEvent, x: Double, y: Double): EditInputEventContext =
        EditInputEventContext(editor = editor, mouseEvent = e, x = x, y = y, readonly = !editor.view.editable)

    protected fun updateCursor(component: Drawable?) {
        if (component == null) {
            editor.view.setCursor(Cursor.DEFAULT)
        } else {
            editor.view.setCursor(Cursor.MOVE)
        }
    }
}