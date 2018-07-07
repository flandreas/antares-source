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
import ch.scorpion.jabbah.edit.CommandManagerActiveEvent
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
        eventBus.register(CommandEvent::class) { update(it) }
	    eventBus.register(CommandManagerActiveEvent::class) { updateEnabledness() }
    }

    override fun execute(event: ActionEvent) {
        commandManager.undo()
    }

    private fun update(event: CommandEvent) {
        if (event.commandManager != this.commandManager) {
            return
        }
        updateEnabledness()
        val desc = commandManager.getUndoDescription()
	    name = if (desc != null) {
		    Translations.getString("edit.action.undo.name.context", desc)
	    } else {
		    Translations.getString("edit.action.undo.name")
	    }
    }

	private fun updateEnabledness() {
		enabled = commandManager.canUndo() && commandManager.active
	}
}

class RedoAction(
        eventBus: EventBus = BaseModule.eventBus,
        private val commandManager: CommandManager = EditModule.commandManager
) : AbstractAction("edit.action.redo") {

    init {
        enabled = false
        eventBus.register(CommandEvent::class) { update(it) }
	    eventBus.register(CommandManagerActiveEvent::class) { updateEnabledness() }
    }

    override fun execute(event: ActionEvent) {
        commandManager.redo()
    }

    private fun update(event: CommandEvent) {
        if (event.commandManager != this.commandManager) {
            return
        }
        updateEnabledness()
        val desc = commandManager.getRedoDescription()
	    name = if (desc != null) {
		    Translations.getString("edit.action.redo.name.context", desc)
	    } else {
		    Translations.getString("edit.action.redo.name")
	    }
    }

	private fun updateEnabledness() {
		enabled = commandManager.canRedo() && commandManager.active
	}
}