package io.antarescircuit.jabbah.edit.tool

import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.Editor

class InputEventHandlerTool(
    private val handler: InputEventHandler<EditInputEventContext>,
    editor: Editor,
    private val activateHandler: () -> Unit
) : AbstractTool(editor) {

    override fun activate() {
        activateHandler()
    }

    override fun deactivate() {
        Status.set(StatusType.Tool, null)
    }

    override fun mouseClicked(e: MouseEvent, x: Double, y: Double) {
        handler.mouseClicked(mouseEventContext(e, x, y))
    }

    override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
        handler.mousePressed(mouseEventContext(e, x, y))
    }

    override fun mouseReleased(e: MouseEvent, x: Double, y: Double) {
        handler.mouseReleased(mouseEventContext(e, x, y))
    }

    override fun mouseMoved(e: MouseEvent, x: Double, y: Double) {
        handler.mouseMoved(mouseEventContext(e, x, y))
    }

    override fun mouseDragged(e: MouseEvent, x: Double, y: Double) {
        handler.mouseDragged(mouseEventContext(e, x, y))
    }

    override fun keyTyped(e: KeyEvent) { }

    override fun keyPressed(e: KeyEvent) {
        handler.keyPressed(keyEventContext(e))
    }

    override fun keyReleased(e: KeyEvent) {
        handler.keyReleased(keyEventContext(e))
    }
}