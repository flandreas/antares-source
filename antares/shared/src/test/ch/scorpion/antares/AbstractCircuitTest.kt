package ch.scorpion.antares

import ch.scorpion.antares.model.gate.CurrentUndefinedGateInputBehavior
import ch.scorpion.antares.model.gate.UndefinedGateInputBehavior
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.math.MILLION
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.scheduler.TimedSchedulerTask
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.assertTrue

/**
 * A test base class for testing Antares circuit simulations.
 * TODO Eliminate copy/paste from corresponding class in graph module
 */
abstract class AbstractCircuitTest {

	private val issueCollector = IssueCollector()
	private lateinit var currentSystemSpeedCategory: CurrentSystemSpeedCategory
	protected lateinit var styleProvider: StyleProvider
	protected lateinit var eventBus: EventBus
	protected lateinit var timeService: ControlledTimeService
	private lateinit var timer: Timer
	protected lateinit var scheduler: SchedulerImpl
	private lateinit var task: TimedSchedulerTask

	@BeforeTest
	open fun setup() {
		AntaresTestRule.configure()

		styleProvider = DrawStyleModule.styleProvider
		eventBus = BaseModule.eventBus
		currentSystemSpeedCategory = CurrentSystemSpeedCategory(SystemSpeed(eventBus = eventBus), eventBus)
		timeService = ControlledTimeService()
		timer = ControlledTimer(timeService)
		task = TimedSchedulerTask(CurrentSystemSpeedCategory(SystemSpeed()), timer)
		scheduler = SchedulerImpl(currentSystemSpeedCategory, timeService, eventBus, NoiseGeneratorHolder(NoNoiseGenerator()), task = task)
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0
	}

	abstract fun getCircuitView(): GraphView

	protected fun startSimulation(proceedTo: Long = 0) {
		scheduler.isActive = true
		LibraryModule.libraryHolder.l?.let { getCircuitView().graph!!.bind(true, it) }
		getCircuitView().checkDesign(scheduler, eventBus)
		getCircuitView().graph!!.formNet(scheduler)
		getCircuitView().graph!!.executionInitialize(scheduler)
		getCircuitView().graph!!.executionStart(scheduler, getCircuitView())
		getCircuitView().executionStart(scheduler)
		if (proceedTo > 0) {
			proceedToNanos(proceedTo)
		}
	}

	protected fun stopSimulation() {
		scheduler.isActive = false
		getCircuitView().graph!!.executionStopped(scheduler)
		timeService.reset()
	}

	protected fun proceedUntilQueueIsEmpty(actorListener: ActorListener = mockk(), maxIterationCount: Int = 1_000) {
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener, maxIterationCount)
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

	protected fun assertNoIssues() {
		assertTrue(issueCollector.issues.isEmpty(), "A simulation issue occurred")
	}
}