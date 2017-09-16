package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.editor.RotateCommand
import java.awt.event.ActionEvent

/**
 * An [Action] for rotating the selected [Component] of a [Drawing].
 */
class RotateAction(
        viewManager: ViewManager,
        val commandManager: CommandManager,
        eventBus: EventBus
) : AbstractSelectionAwareAction("edit.action.rotate", viewManager, eventBus) {

    override fun actionPerformed(e: ActionEvent?) {
        commandManager.execute(RotateCommand(getSingleSelection()!!, getSingleSelection()!!.rotation.next()))
    }

    override fun calculateEnabled(): Boolean {
        return getSelectionCount() == 1 && getSingleSelection()!!.rotatable
    }
}