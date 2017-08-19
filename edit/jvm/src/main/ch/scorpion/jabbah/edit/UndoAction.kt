package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import java.awt.event.ActionEvent
import javax.swing.Action

/**
 * An [Action] for undoing the las [Command] transaction in a [CommandManager].
 */
class UndoAction(
        eventBus: EventBus,
        private val commandManager: CommandManager
) : AbstractAction("edit.action.undo") {

    init {
        isEnabled = false
        eventBus.register(CommandEvent::class, {update(it)})
    }

    override fun actionPerformed(e: ActionEvent?) {
        commandManager.undo()
    }

    private fun update(event: CommandEvent) {
        if (event.commandManager != this.commandManager) {
            return
        }
        isEnabled = commandManager.canUndo()
        val desc = commandManager.getUndoDescription()
        if (desc != null) {
            name = Translations.getString("edit.action.undo.name.context", desc)
        } else {
            name = Translations.getString("edit.action.undo.name")
        }
    }
}