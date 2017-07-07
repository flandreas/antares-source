package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * Changes the text of a [TextComponent].
 */
class TextChangeCommand(
    editor: Editor,
    private val component: TextComponent,
    private val oldText: String,
    private val newText: String
) : AbstractCommand("edit.command.textChange", editor) {

    override fun execute() {
        component.text = newText
    }

    override fun undo() {
        component.text = oldText
    }
}