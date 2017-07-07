package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.AbstractCommand

@Suppress("unused")
/**
 * A [Command] that cuts the selected [Component]s from the [Drawing].
 * Note that this [Command] does NOT access the clipboard. It it only a wrapper
 * around a [DeleteCommand] in order to provide another command name.
 */
class CutCommand(
    drawingView: DrawingView<Drawing<Component>>,
    components: List<Component>
) : AbstractCommand("graph.command.cut", null) {

    private val deleteCmd = DeleteCommand(drawingView, components)

    override fun execute() {
        deleteCmd.execute()
    }

    override fun undo() {
        deleteCmd.undo()
    }
}