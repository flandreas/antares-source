package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Disposable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.CommandManagerActiveEvent
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * A delegate used in [Action] implementations to disable its host [Action] as long as
 * [CommandManager.active] is `false`.
 */
class ActiveCommandManagerAction(
    private val action: Action,
    private val commandManager: CommandManager = EditModule.commandManager,
    private val eventBus: EventBus = BaseModule.eventBus
) : Disposable {

    private val commandManagerActiveHandler: EventHandler<CommandManagerActiveEvent> = { action.updateEnabled() }

    val enabled: Boolean get() = commandManager.active

    init {
        eventBus.register(CommandManagerActiveEvent::class, commandManagerActiveHandler)
    }

    override fun dispose() {
        eventBus.unregister(commandManagerActiveHandler)
    }
}