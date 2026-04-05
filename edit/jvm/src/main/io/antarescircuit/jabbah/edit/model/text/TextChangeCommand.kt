package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand

/**
 * Changes the text of a [TextComponent].
 */
class TextChangeCommand(
    editor: Editor,
    private val componentId: Int,
    private val oldText: Translatable,
    private val newText: Translatable
) : AbstractCommand("edit.command.textChange", editor), Undoable {

	private val component: TextComponent get() = editor!!.drawing.getWithId(componentId)!!.propertyOwner as TextComponent

    override fun execute() {
        component.text = newText
    }

    override fun undo() {
        component.text = oldText
    }
}