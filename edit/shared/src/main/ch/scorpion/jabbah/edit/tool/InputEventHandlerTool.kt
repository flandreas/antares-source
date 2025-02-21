package ch.scorpion.jabbah.edit.tool

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor

class InputEventHandlerTool(
    private val handler: InputEventHandler<EditInputEventContext>,
    editor: Editor,
    private val activateHandler: () -> Unit
) : AbstractTool(editor) {

    override fun activate() {
        activateHandler()
    }

    override fun deactivate() {}

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