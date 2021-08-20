package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.animation.AnimatorImpl
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.draw.ApplicationContextHolder
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerRunningStateEvent
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder

/**
 * A graph application sets an instance of [GraphApplicationContext] as the application context
 * in the [View]s it displays.
 */
data class GraphApplicationContext(
	val systemSpeedCategory: CurrentSystemSpeedCategory,
	val mode: ApplicationMode = ApplicationMode.EDIT,
	val isPausing: Boolean = false
) {
	val isExecute: Boolean get() = mode.isExecute()

	val showNetState: Boolean get() = isExecute && (isPausing || systemSpeedCategory.systemSpeedCategory > SystemSpeedCategory.Use)
}

class GraphApplicationContextHolder(
	val scheduler: Scheduler,
	private val eventBus: EventBus = BaseModule.eventBus,
	val systemSpeed: SystemSpeed = SystemSpeed(eventBus),
	val currentSystemSpeedCategory: CurrentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed),
	val animator: Animator = AnimatorImpl(systemSpeed)
) : ApplicationContextHolder() {

	private val applicationMode: ApplicationMode get() = applicationModeHolder.currentMode
	private val systemSpeedHandler: (SystemSpeedEvent) -> Unit = {
		if (it.source === systemSpeed) {
			updateApplicationContext()
		}
	}
	private val applicationModeEventHandler: (ApplicationModeEvent) -> Unit = {
		if (it.source === applicationModeHolder) {
			updateApplicationContext()
		}
	}
	private val schedulerRunningStateHandler: (SchedulerRunningStateEvent) -> Unit = { updateApplicationContext() }

	/** Variable due to cyclic redundancy.*/
	lateinit var applicationModeHolder: ApplicationModeHolder

	init {
		eventBus.register(SystemSpeedEvent::class, systemSpeedHandler)
		eventBus.register(ApplicationModeEvent::class, applicationModeEventHandler)
		eventBus.register(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
		applicationContext = GraphApplicationContext(currentSystemSpeedCategory, ApplicationMode.EDIT, scheduler.isPaused)
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
		GraphApplicationContext(currentSystemSpeedCategory, applicationMode,  scheduler.isPaused)
}