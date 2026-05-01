package io.antarescircuit.antares

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.antares.model.gate.CurrentUndefinedGateInputBehavior
import io.antarescircuit.antares.model.gate.UndefinedGateInputBehavior
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.math.MILLION
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.ControlledTimeService
import io.antarescircuit.jabbah.base.time.ControlledTimer
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.time.Timer
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.execution.issue.IssueCollector
import io.antarescircuit.jabbah.execution.noise.NoNoiseGenerator
import io.antarescircuit.jabbah.execution.noise.NoiseGeneratorHolder
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl
import io.antarescircuit.jabbah.execution.scheduler.TimedSchedulerTask
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.ui.GraphViewUI
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewExecutionController
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

	private lateinit var executionController: GraphViewExecutionController

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

		val drawingViewBuilder = DrawingViewMockBuilder().withDrawingAccessor(::getCircuitView)
		val graphViewUI = mock<GraphViewUI>(MockMode.autofill)
		every { graphViewUI.drawingView } returns drawingViewBuilder.build()

		executionController = GraphViewExecutionController(
			graphViewUI = graphViewUI,
			isRoot = true,
			rootGraphProvider = { getCircuitView().graph},
			graphViewsProvider = { listOf(getCircuitView()) },
			applicationContextHolder = GraphApplicationContextHolder(scheduler, eventBus),
			eventBus = eventBus,
		)
	}

	abstract fun getCircuitView(): GraphView

	protected fun startSimulation(proceedTo: Long = 0) {
		scheduler.isActive = true
		LibraryModule.libraryHolder.l?.let { getCircuitView().graph!!.bind(true, it) }

		getCircuitView().checkDesign(scheduler, eventBus)

		if (proceedTo > 0) {
			proceedToNanos(proceedTo)
		}
	}

	protected fun stopSimulation() {
		scheduler.isActive = false
		getCircuitView().graph!!.executionStopped(scheduler)
		timeService.reset()
	}

	protected fun proceedUntilQueueIsEmpty(actorListener: ActorListener = mock(), maxIterationCount: Int = 1_000) {
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
		assertTrue(issueCollector.issues.isEmpty(), "A simulation issue occurred: ${issueCollector.issues.firstOrNull()?.description }")
	}
}