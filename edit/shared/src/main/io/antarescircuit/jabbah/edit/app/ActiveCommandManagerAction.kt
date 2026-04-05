package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Disposable
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.CommandManagerActiveEvent
import io.antarescircuit.jabbah.edit.module.EditModule

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