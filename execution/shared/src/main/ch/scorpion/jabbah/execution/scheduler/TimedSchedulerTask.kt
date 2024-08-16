package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.ActionListener
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.math.interpolate
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory.*

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
		var count = 0
		lateinit var result: ExecutionStepResult
		do {
			result = scheduler.execute()
			count++
		} while (!result.breakpoint && count < number && !scheduler.isQueueEmpty && result.recalculated)
	}
}