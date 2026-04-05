package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.CommandEvent
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.module.EditModule

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