package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.draw.ApplicationContextHolder
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.scheduler.SchedulerRunningStateEvent
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent

/**
 * A graph application sets an instance of [GraphApplicationContext] as the application context
 * in the [View]s it displays.
 */
data class GraphApplicationContext(
	val mode: ApplicationMode = ApplicationMode.EDIT,
	val systemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	val isPausing: Boolean = false
) {
	val isExecute: Boolean get() = mode.isExecute()

	val showNetState: Boolean get() = isExecute && (isPausing || systemSpeedCategory.systemSpeedCategory > SystemSpeedCategory.Use)
}

class GraphApplicationContextHolder(
	val scheduler: Scheduler = SchedulerImpl(),
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	private val eventBus: EventBus = BaseModule.eventBus
) : ApplicationContextHolder() {

	private val applicationMode: ApplicationMode get() = if (scheduler.isActive) ApplicationMode.EXECUTE else ApplicationMode.EDIT

	private val systemSpeedHandler: (SystemSpeedEvent) -> Unit = { updateApplicationContext() }
	private val applicationModeEventHandler: (ApplicationModeEvent) -> Unit = { updateApplicationContext() }
	private val schedulerRunningStateHandler: (SchedulerRunningStateEvent) -> Unit = { updateApplicationContext() }

	init {
		eventBus.register(SystemSpeedEvent::class, systemSpeedHandler)
		eventBus.register(ApplicationModeEvent::class, applicationModeEventHandler)
		eventBus.register(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
		applicationContext = createApplicationContext()
	}

	override fun dispose() {
		eventBus.unregister(SystemSpeedEvent::class, systemSpeedHandler)
		eventBus.unregister(ApplicationModeEvent::class, applicationModeEventHandler)
		eventBus.unregister(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
	}

	private fun updateApplicationContext() {
		applicationContext = createApplicationContext()
	}

	private fun createApplicationContext(): GraphApplicationContext =
		GraphApplicationContext(applicationMode, currentSystemSpeedCategory, scheduler.isPaused)
}