package io.antarescircuit.jabbah.app.health

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.app.SystemMalfunctionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.CommandEvent
import io.antarescircuit.jabbah.edit.CommandEventType

/**
 * Implements any logic for checking the system's health, such as
 * inconsistencies in the current [ApplicationData]. Implementations
 * can be registered  with [SystemHealthChecker] during system startup.
 */
interface SystemHealthCheck {
	fun execute(data: ApplicationData): SystemMalfunctionEvent?
}

/**
 * Executes all registered [SystemHealthCheck]s upon every [CommandEvent],
 * and posts the first found [SystemMalfunctionEvent] on the system's [EventBus]
 * to ask the user to open a GitHub issue and attach the exported system dump.
 */
object SystemHealthChecker {

	private val LOG by logger(SystemHealthCheck::class)

	private val checks = mutableListOf<SystemHealthCheck>()

	private val commandListener: EventHandler<CommandEvent> = {
		if (it.type != CommandEventType.RESET) {
			handle(it)
		}
	}

	private lateinit var applicationDataHolder: ApplicationDataHolder

	fun register(check: SystemHealthCheck) {
		LOG.info("Activating ${check::class.simpleName}")
		checks.add(check)
	}

	fun start(applicationDataHolder: ApplicationDataHolder) {
		SystemHealthChecker.applicationDataHolder = applicationDataHolder
		BaseModule.eventBus.register(CommandEvent::class, commandListener)
	}

	@Suppress("unused")
	fun stop() {
		BaseModule.eventBus.unregister(commandListener)
	}

	fun handle(@Suppress("UNUSED_PARAMETER") event: CommandEvent) {
		applicationDataHolder.data?.let { data ->
			checks.firstNotNullOfOrNull { it.execute(data) }?.let {
				BaseModule.eventBus.post(it)
			}
		}
	}
}