package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * An [Action] for undoing the last [Command] transaction in a [CommandManager].
 */
class UndoAction(
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractEditAction("edit.action.undo") {

	private val commandHandler: EventHandler<CommandEvent> = { update(it) }

	init {
		eventBus.register(CommandEvent::class, commandHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(commandHandler)
	}

	override fun execute(event: ActionEvent) {
		commandManager.undo()
	}

	private fun update(event: CommandEvent) {
		if (event.commandManager != this.commandManager) {
			return
		}
		updateEnabled()
		val desc = commandManager.getUndoDescription()
		name = if (desc != null) {
			Translations.getString("edit.action.undo.name.context", desc)
		} else {
			Translations.getString("edit.action.undo.name")
		}
	}

	override fun calculateEnabled(): Boolean = super.calculateEnabled()
		&& commandManager.canUndo()
}

/**
 * An [Action] for redoing the last undone [Command] transaction in a [CommandManager].
 */
class RedoAction(
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractEditAction("edit.action.redo") {

	private val commandHandler: EventHandler<CommandEvent> = { update(it) }

	init {
		eventBus.register(CommandEvent::class, commandHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(commandHandler)
	}

	override fun execute(event: ActionEvent) {
		commandManager.redo()
	}

	private fun update(event: CommandEvent) {
		if (event.commandManager != this.commandManager) {
			return
		}
		updateEnabled()
		val desc = commandManager.getRedoDescription()
		name = if (desc != null) {
			Translations.getString("edit.action.redo.name.context", desc)
		} else {
			Translations.getString("edit.action.redo.name")
		}
	}

	override fun calculateEnabled(): Boolean = super.calculateEnabled()
		&& commandManager.canRedo()
}