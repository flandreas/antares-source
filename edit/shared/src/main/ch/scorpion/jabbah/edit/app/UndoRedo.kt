package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * An [Action] for undoing the las [Command] transaction in a [CommandManager].
 */
class UndoAction(
    eventBus: EventBus = BaseModule.eventBus,
    private val commandManager: CommandManager = EditModule.commandManager
) : AbstractAction("edit.action.undo") {

    init {
        enabled = false
        eventBus.register(CommandEvent::class, { update(it) })
    }

    override fun execute(event: ActionEvent) {
        commandManager.undo()
    }

    private fun update(event: CommandEvent) {
        if (event.commandManager != this.commandManager) {
            return
        }
        enabled = commandManager.canUndo()
        val desc = commandManager.getUndoDescription()
        if (desc != null) {
            name = Translations.getString("edit.action.undo.name.context", desc)
        } else {
            name = Translations.getString("edit.action.undo.name")
        }
    }
}

class RedoAction(
        eventBus: EventBus = BaseModule.eventBus,
        private val commandManager: CommandManager = EditModule.commandManager
) : AbstractAction("edit.action.redo") {

    init {
        enabled = false
        eventBus.register(CommandEvent::class, { update(it) })
    }

    override fun execute(event: ActionEvent) {
        commandManager.redo()
    }

    private fun update(event: CommandEvent) {
        if (event.commandManager != this.commandManager) {
            return
        }
        enabled = commandManager.canRedo()
        val desc = commandManager.getRedoDescription()
        if (desc != null) {
            name = Translations.getString("edit.action.redo.name.context", desc)
        } else {
            name = Translations.getString("edit.action.redo.name")
        }
    }
}