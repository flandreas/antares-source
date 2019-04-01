package ch.scorpion.antares

import ch.scorpion.jabbah.base.MILLION
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.IOModule
import kotlin.test.BeforeTest

/**
 * A test base class for testing Antares circuit simulations.
 */
abstract class AbstractCircuitTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	protected lateinit var styleProvider: StyleProvider
	protected lateinit var eventBus: EventBus
	protected lateinit var timeService: ControlledTimeService
	protected lateinit var timer: Timer
	protected lateinit var scheduler: Scheduler

	@BeforeTest
	open fun setup() {
		styleProvider = DrawStyleModule.styleProvider
		eventBus = BaseModule.eventBus
		timeService = ControlledTimeService()
		timer = ControlledTimer(timeService)
		scheduler = SchedulerImpl(timeService, timer, eventBus, NoiseGeneratorHolder(NoNoiseGenerator()))
	}

	abstract fun getCircuitView(): GraphView<GraphElementView<*>>

	protected fun startSimulation() {
		scheduler.isActive = true
		LibraryModule.libraryHolder.l?.let { getCircuitView().graph!!.bind(it, IOModule.storableCreator) }
		getCircuitView().graph!!.executionStarted(scheduler)
	}

	protected fun stopSimulation() {
		scheduler.isActive = false
		getCircuitView().graph!!.executionStopped(scheduler)
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
	protected fun proceedFrozenTimeTo(time: Long) {
		timeService.setTimeMillis(time)
		timeService.setTimeMillis(time + scheduler.timerInterval + 1)
	}
}