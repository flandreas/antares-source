package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * A [Command] for adding a [Component] to a [Drawing].
 */
class AddCommand(
    editor: Editor,
    val component: Component
) : AbstractCommand("edit.command.add", editor){

    override fun execute() {
        editor!!.drawing.add(component)
    }

    override fun undo() {
        editor!!.drawing.remove(component)
    }
}