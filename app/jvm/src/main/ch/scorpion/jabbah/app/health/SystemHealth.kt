package ch.scorpion.jabbah.app.health

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.app.SystemMalfunctionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandEvent

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

	private val commandListener: EventHandler<CommandEvent> = { handle(it) }

	private lateinit var applicationDataHolder: ApplicationDataHolder

	fun register(check: SystemHealthCheck) {
		LOG.info("Activating ${check::class.simpleName}")
		checks.add(check)
	}

	fun start(applicationDataHolder: ApplicationDataHolder) {
		this.applicationDataHolder = applicationDataHolder
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