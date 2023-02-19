package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.ActionListener
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory.Explore

/** A [SchedulerTask] that is driven by a [Timer]. */
class TimedSchedulerTask(
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory,
	private val timer: Timer = System.createTimer(),
	eventBus: EventBus = BaseModule.eventBus,
) : AbstractSchedulerTask("execution.task.timed"), ActionListener {

	companion object {

		private val LOG by logger(TimedSchedulerTask::class)

		// Tuning: Avoid costly System.currentTimeMillis(). Value has been found experimentally.
		private const val STEPS_PER_20_MILLISECOND = 20_000
		private const val STEPS_NON_MAX = 1_000

		private const val INFINITE_DELAY = Int.MAX_VALUE
		private const val MIN_DELAY = 1
		private const val MAX_DELAY = 1_000
		private const val THIRD_DELAY = 50

		const val DEF_SLOWDOWN_FACTOR = 4.0f

		/**
		 * The name of the [Float] value in [Properties] that determines how much the simulation is slowed dow
		 * in relation to the [CurrentSystemSpeedCategory]' [SystemSpeed].
		 * */
		const val PROP_SLOWDOWN_FACTOR = "TimedSchedulerTask.slowDownFactor"
	}

	init {
		eventBus.register(SystemSpeedEvent::class) {
			if (it.source === currentSystemSpeedCategory.systemSpeed) {
				adaptToSystemSpeed()
			}
		}
		timer.initialize(calculateTimerInterval()) { actionPerformed(it) }
	}

	private lateinit var scheduler: Scheduler

	private var slowDownFactor: Float = DEF_SLOWDOWN_FACTOR

	/** ---- [SchedulerTask] interface */

	override fun startIfNeeded() {
		if (!scheduler.isQueueEmpty && !timer.isRunning()) {
			LOG.trace("Starting timer")
			slowDownFactor = BaseModule.properties.getFloat(PROP_SLOWDOWN_FACTOR)
			adaptToSystemSpeed()
			timer.start()
		}
	}

	override fun bind(scheduler: Scheduler) {
		this.scheduler = scheduler
	}

	override fun stop() {
		LOG.trace("Stopping timer")
		timer.stop()
	}

	/** ---- [ActionListener] */

	/**
	 * Called by the [Timer] that drives this [TimedSchedulerTask].
	 *
	 * Due to the types in the interface of a [Timer], the interval of a [Timer] can't be smaller than 1 ms.
	 * If the system should run at maximum speed, this interval is too long. We therefore perform more than
	 * one execution step at a single timer tick.
	 */
	override fun actionPerformed(event: ActionEvent) {
		if (currentSystemSpeedCategory.systemSpeed.isMaximum) {
			executeNumberOfSteps(STEPS_PER_20_MILLISECOND)
		} else {
			executeNumberOfSteps(STEPS_NON_MAX)
		}
	}

	private fun executeNumberOfSteps(number: Int) {
		var count = 0
		lateinit var result: ExecutionStepResult
		do {
			result = scheduler.execute()
			count++
		} while (!result.breakpoint && count < number && !scheduler.isQueueEmpty)
	}

	/** ---- [TimedSchedulerTask] */

	private fun calculateTimerInterval(): Int {
		val speed = currentSystemSpeedCategory.systemSpeed
		val interval = if (speed.speed == 0) {
			INFINITE_DELAY
		} else if (speed.isMaximum) {
			MIN_DELAY
		} else {
			if (currentSystemSpeedCategory.systemSpeedCategory == Explore) {
				(slowDownFactor * (MAX_DELAY - speed.speed.toFloat() / Explore.speedRange.last * (MAX_DELAY - THIRD_DELAY))).toInt()
			} else {
				(slowDownFactor * (THIRD_DELAY - speed.speed.toFloat() / SystemSpeed.MAX_SPEED * THIRD_DELAY)).toInt()
			}
		}
		LOG.trace("speed = ${currentSystemSpeedCategory.systemSpeed.speed}, interval = $interval, slowDownFactor = $slowDownFactor")
		return interval
	}

	private fun adaptToSystemSpeed() {
		val newInterval = calculateTimerInterval()
		val needRestart = timer.interval == Int.MAX_VALUE && newInterval != Int.MAX_VALUE && timer.isRunning()
		timer.interval = newInterval
		if (needRestart) {
			timer.stop()
			timer.start()
		}
	}
}