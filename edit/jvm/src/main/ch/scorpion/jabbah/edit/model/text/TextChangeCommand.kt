package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * Changes the text of a [TextComponent].
 */
class TextChangeCommand(
    editor: Editor,
    private val componentId: Int,
    private val oldText: String,
    private val newText: String
) : AbstractCommand("edit.command.textChange", editor), Undoable {

	private val component: TextComponent get() = editor!!.drawing.getWithId(componentId)!!.propertyOwner as TextComponent

    override fun execute() {
        component.text = newText
    }

    override fun undo() {
        component.text = oldText
    }
}