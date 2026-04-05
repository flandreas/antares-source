package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.math.MILLION
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.ControlledTimeService
import io.antarescircuit.jabbah.base.time.ControlledTimer
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.time.Timer
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.execution.noise.NoNoiseGenerator
import io.antarescircuit.jabbah.execution.noise.NoiseGeneratorHolder
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl
import io.antarescircuit.jabbah.execution.scheduler.TimedSchedulerTask
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.mock
import kotlin.test.BeforeTest

/**
 * TODO Eliminate copy/paste from corresponding class in antares module.
 */
abstract class AbstractGraphViewExecutionTest {

	protected lateinit var currentSystemSpeedCategory: CurrentSystemSpeedCategory
	protected lateinit var styleProvider: StyleProvider
	protected lateinit var eventBus: EventBus
	protected lateinit var timeService: ControlledTimeService
	protected lateinit var timer: Timer
	protected lateinit var scheduler: SchedulerImpl
	protected lateinit var task: TimedSchedulerTask

	@BeforeTest
	open fun setup() {
		GraphViewTestRule.configure()
		styleProvider = DrawStyleModule.styleProvider
		eventBus = BaseModule.eventBus
		currentSystemSpeedCategory = CurrentSystemSpeedCategory(SystemSpeed(eventBus = eventBus), eventBus)
		timeService = ControlledTimeService()
		timer = ControlledTimer(timeService)
		task = TimedSchedulerTask(CurrentSystemSpeedCategory(SystemSpeed()), ControlledTimer(timeService))
		scheduler = SchedulerImpl(currentSystemSpeedCategory, timeService, eventBus, NoiseGeneratorHolder(NoNoiseGenerator()), task = task)
	}

	abstract fun getGraphView(): GraphView

	protected fun startSimulation(proceedTo: Long = 0) {
		scheduler.isActive = true
		LibraryModule.libraryHolder.l?.let { getGraphView().graph!!.bind(true, it) }
		getGraphView().graph!!.formNet(scheduler)
		getGraphView().graph!!.executionInitialize(scheduler)
		getGraphView().graph!!.executionStart(scheduler, getGraphView())
		getGraphView().executionStart(scheduler)
		if (proceedTo > 0) {
			proceedToNanos(proceedTo)
		}
	}

	protected fun stopSimulation() {
		scheduler.isActive = false
		getGraphView().graph!!.executionStopped(scheduler)
	}

	protected fun proceedUntilQueueIsEmpty(actorListener: ActorListener = mock()) {
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
	}

	protected fun proceedToMillis(timeMillis: Long) {
		timeService.setTimeMillis(timeMillis)
		scheduler.proceedTo(timeMillis * MILLION)
	}

	protected fun proceedToNanos(timeNanos: Long) {
		timeService.setTimeNanos(timeNanos)
		scheduler.proceedTo(timeNanos)
	}

	/**
	 * If the head of the scheduling queue uses time freezing, the relative time is updated AFTER the [Actor] has acted.
	 * Therefore, we need an additional time tick to continue with the simulation when testing. Note that the second time
	 * tick must be later than the [Scheduler]'s [Timer] interval, because otherwise the [Timer] wouldn't wake up and
	 * the [Scheduler] wouldn't be triggered.
	 */
	protected fun proceedFrozenTimeToMillis(time: Long) {
		timeService.setTimeMillis(time)
		timeService.setTimeMillis(time + timer.interval + 1)
	}

	protected fun proceedFrozenTimeToNanos(time: Long) {
		timeService.setTimeMillis(time / MILLION)
		timeService.setTimeMillis((time + timer.interval + 1) / MILLION)
	}

	protected inner class DummyApplicationModeHolder : ApplicationModeHolder {

		private var _currentMode: ApplicationMode = ApplicationMode.EDIT
		override val currentMode: ApplicationMode get() = _currentMode

		override fun dispose() { }

		override fun setMode(mode: ApplicationMode, after: () -> Unit) {
			if (mode.isExecute()) {
				startSimulation()
			} else {
				stopSimulation()
			}
			after.invoke()
		}

		override fun updateEditorEditability() { }
	}
}