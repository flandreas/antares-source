package io.antarescircuit.jabbah.execution.scheduler

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.ActionListener
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.math.interpolate
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.time.SystemSpeedEvent
import io.antarescircuit.jabbah.base.time.Timer
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory.*
import kotlin.random.Random

/** A [SchedulerTask] that is driven by a [Timer]. */
class TimedSchedulerTask(
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory,
	private val timer: Timer = System.createTimer(),
	private val eventBus: EventBus = BaseModule.eventBus,
) : AbstractSchedulerTask("execution.task.timed"), ActionListener {

	companion object {

		private val LOG by logger(TimedSchedulerTask::class)

		const val MAX_SPEED_STEPS = 10_000

		/**
		 * The name of the [Float] value in [Properties] that determines how much the simulation is slowed dow
		 * in relation to the [CurrentSystemSpeedCategory]' [SystemSpeed].
		 * */
		const val PROP_SLOWDOWN_FACTOR = "TimedSchedulerTask.slowDownFactor"

		const val DEF_SLOWDOWN_FACTOR = 4.0f
	}

	private val stepsMap = mapOf(
		Use to 100..MAX_SPEED_STEPS,
		Observe to 3..100,
		Explore to 1..3
	)

	private val intervalMap = mapOf(
		Use to (5 downTo 1),
		Observe to (10 downTo 5),
		Explore to (30 downTo 10)
	)

	private val systemSpeedHandler: EventHandler<SystemSpeedEvent> = {
		if (it.source == currentSystemSpeedCategory.systemSpeed) {
			adaptToSystemSpeed()
		}
	}

	private val slowDownFactor: Float by lazy { BaseModule.properties.getFloat(PROP_SLOWDOWN_FACTOR) }

	private lateinit var scheduler: Scheduler

	private var numberOfSteps: Int

	private var timerInterval: Int

	init {
		eventBus.register(SystemSpeedEvent::class, systemSpeedHandler)

		numberOfSteps = calculateNumberOfSteps()
		timerInterval = calculateTimerInterval()

		timer.initialize(timerInterval) { actionPerformed(it) }
	}

	fun dispose() {
		eventBus.unregister(systemSpeedHandler)
	}

	/** ---- [SchedulerTask] interface */

	override fun bind(scheduler: Scheduler) {
		this.scheduler = scheduler
	}

	override fun startIfNeeded() {
		if (!scheduler.isQueueEmpty && !timer.isRunning()) {
			LOG.trace("Starting timer")
			adaptToSystemSpeed()
			timer.start()
		}
	}

	override fun stop() {
		timer.stop()
	}

	/** ---- [ActionListener] interface */

	override fun actionPerformed(event: ActionEvent) {
		executeNumberOfSteps(numberOfSteps)
	}

	/** ---- [TimedSchedulerTask] */

	private fun calculateTimerInterval(): Int {
		val category = currentSystemSpeedCategory.systemSpeedCategory
		val speed = currentSystemSpeedCategory.systemSpeed.speed

		return if (currentSystemSpeedCategory.systemSpeed.isMaximum) {
			1
		} else {
			val intervals = intervalMap[category]!!
			(slowDownFactor * category.speedRange.interpolate(speed, intervals.first, intervals.last)).toInt()
		}
	}

	private fun calculateNumberOfSteps(): Int {
		val category = currentSystemSpeedCategory.systemSpeedCategory
		val speed = currentSystemSpeedCategory.systemSpeed.speed

		return if (currentSystemSpeedCategory.systemSpeed.isMaximum) {
			MAX_SPEED_STEPS
		} else {
			val steps = stepsMap[category]!!
			category.speedRange.interpolate(speed, steps.first, steps.last)
		}
	}

	private fun adaptToSystemSpeed() {
		numberOfSteps = calculateNumberOfSteps()
		timerInterval = calculateTimerInterval()

		if (LOG.isTraceEnabled()) {
			LOG.trace("number of steps: $numberOfSteps, interval: $timerInterval")
		}

		val needRestart = timer.interval == Int.MAX_VALUE && timerInterval != Int.MAX_VALUE && timer.isRunning()
		timer.interval = timerInterval
		if (needRestart) {
			timer.stop()
			timer.start()
		}
	}

	private fun executeNumberOfSteps(number: Int) {
		// Deviate number randomly to avoid seemingly unchanged display in oscillating models (#1235)
		val effNumber = number + Random.nextInt(0, 3)

		var count = 0
		lateinit var result: ExecutionStepResult
		do {
			result = scheduler.execute()
			count++
		} while (!result.breakpoint && count < effNumber && !scheduler.isQueueEmpty && result.recalculated)
	}
}