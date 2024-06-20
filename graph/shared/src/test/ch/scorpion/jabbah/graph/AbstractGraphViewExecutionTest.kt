package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.math.MILLION
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.scheduler.TimedSchedulerTask
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.mock
import kotlin.test.BeforeTest

/**
 * TODO Eliminate copy/paste from corresponding class in antares module.
 */
abstract class AbstractGraphViewExecutionTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	protected lateinit var currentSystemSpeedCategory: CurrentSystemSpeedCategory
	protected lateinit var styleProvider: StyleProvider
	protected lateinit var eventBus: EventBus
	protected lateinit var timeService: ControlledTimeService
	protected lateinit var timer: Timer
	protected lateinit var scheduler: SchedulerImpl
	protected lateinit var task: TimedSchedulerTask

	@BeforeTest
	open fun setup() {
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