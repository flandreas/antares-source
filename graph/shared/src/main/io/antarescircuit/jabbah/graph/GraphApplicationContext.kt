package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.animation.Animator
import io.antarescircuit.jabbah.animation.AnimatorImpl
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.time.SystemSpeedEvent
import io.antarescircuit.jabbah.draw.ApplicationContextHolder
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerSingleStepModeEvent
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioBreakpoints

/**
 * A graph application sets an instance of [GraphApplicationContext] as the application context
 * in the [View]s it displays.
 */
data class GraphApplicationContext(
	val systemSpeedCategory: CurrentSystemSpeedCategory,
	val mode: ApplicationMode = ApplicationMode.EDIT,
	val isPausing: Boolean = false
) {

	companion object {
		fun isShowNetState(scheduler: Scheduler): Boolean =
			// TODO: Combine with equivalent property below
			scheduler.isActive && (scheduler.isSingleStepMode || scheduler.systemSpeedCategory.systemSpeedCategory > SystemSpeedCategory.Use)
	}
	val isExecute: Boolean get() = mode.isExecute()

	val showNetState: Boolean get() = isExecute && (isPausing || systemSpeedCategory.systemSpeedCategory > SystemSpeedCategory.Use)
}

class GraphApplicationContextHolder(
	val scheduler: Scheduler,
	val eventBus: EventBus = BaseModule.eventBus,
	val systemSpeed: SystemSpeed = SystemSpeed(eventBus = eventBus),
	val currentSystemSpeedCategory: CurrentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed),
	val animator: Animator = AnimatorImpl(systemSpeed),
	val scenarioBreakpoints: ScenarioBreakpoints = ScenarioBreakpoints(eventBus)
) : ApplicationContextHolder() {

	val signalHandlerIfActive: SignalHandler? get() = if (scheduler.isActive) scheduler else null

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
	private val schedulerRunningStateHandler: (SchedulerSingleStepModeEvent) -> Unit = { updateApplicationContext() }

	/** Variable due to cyclic redundancy.*/
	lateinit var applicationModeHolder: ApplicationModeHolder

	init {
		eventBus.register(SystemSpeedEvent::class, systemSpeedHandler)
		eventBus.register(ApplicationModeEvent::class, applicationModeEventHandler)
		eventBus.register(SchedulerSingleStepModeEvent::class, schedulerRunningStateHandler)
		applicationContext = GraphApplicationContext(currentSystemSpeedCategory, ApplicationMode.EDIT, scheduler.isSingleStepMode)
	}

	override fun dispose() {
		animator.dispose()
		eventBus.unregister(SystemSpeedEvent::class, systemSpeedHandler)
		eventBus.unregister(ApplicationModeEvent::class, applicationModeEventHandler)
		eventBus.unregister(SchedulerSingleStepModeEvent::class, schedulerRunningStateHandler)
	}

	private fun updateApplicationContext() {
		applicationContext = createApplicationContext()
	}

	private fun createApplicationContext(): GraphApplicationContext =
		GraphApplicationContext(currentSystemSpeedCategory, applicationMode,  scheduler.isSingleStepMode)
}