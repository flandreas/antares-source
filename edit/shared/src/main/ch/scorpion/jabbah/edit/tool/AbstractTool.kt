package ch.scorpion.jabbah.edit.tool

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool

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