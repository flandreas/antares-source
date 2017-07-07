package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
/**
 * Posted by a [CommandManager] on its [EventBus] whenever its state has changed.
 */
data class CommandEvent(val commandManager: CommandManager)