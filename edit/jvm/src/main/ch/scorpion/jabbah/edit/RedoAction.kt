package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import java.awt.event.ActionEvent

/**
 * An [Action] for redoing the las [Command] transaction in a [CommandManager].
 */
class RedoAction(
        eventBus: EventBus,
        private val commandManager: CommandManager
) : AbstractAction("edit.action.redo") {

    init {
        isEnabled = false
        eventBus.register(CommandEvent::class, {update(it)})
    }

    override fun actionPerformed(e: ActionEvent?) {
        commandManager.redo()
    }

    private fun update(event: CommandEvent) {
        if (event.commandManager != this.commandManager) {
            return
        }
        isEnabled = commandManager.canRedo()
        val desc = commandManager.getRedoDescription()
        if (desc != null) {
            setName(Translations.getString(getNameContextKey(), desc))
        } else {
            setName(Translations.getString(getNameKey()))
        }
    }

    private fun getNameContextKey(): String {
        return "${getNameKey()}.context"
    }
}