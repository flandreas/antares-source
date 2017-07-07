package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.editor.DeleteCommand
import java.awt.event.ActionEvent

/**
 * An [Action] for deleting the selected [Component]s in a [Drawing].
 */
    class DeleteAction(viewManager: ViewManager, val commandManager: CommandManager) : AbstractSelectionAwareAction("edit.action.delete", viewManager){

    override fun actionPerformed(e: ActionEvent?) {
        val drawingView = viewManager.activeView as DrawingView<Drawing<Component>>
        commandManager.beginTransaction(DeleteCommand(
                drawingView,
                drawingView.selectionManager.selection
                        .filter { it.deletable }
                        .toCollection(mutableListOf<Component>())))
        commandManager.commitTransaction()
    }
}