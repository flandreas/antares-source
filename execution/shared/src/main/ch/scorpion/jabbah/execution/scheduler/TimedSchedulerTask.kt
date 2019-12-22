package ch.scorpion.jabbah.execution.scheduler

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.ActionListener
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.math.max

/** A [SchedulerTask] that is driven by a [Timer]. */
class TimedSchedulerTask(
	private val timer: Timer = System.createTimer(),
	eventBus: EventBus = BaseModule.eventBus,
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory
) : AbstractSchedulerTask("execution.task.timed"), ActionListener {

	companion object {
		private val LOG by logger(TimedSchedulerTask::class)
		private const val SLOWDOWN_FACTOR = 0.5
	}

	init {
		eventBus.register(SystemSpeedEvent::class) { adaptToSystemSpeed() }
		timer.initialize(calculateTimerInterval()) { actionPerformed(it) }
	}

	private lateinit var scheduler: Scheduler

	/** ---- [SchedulerTask] interface */

	override fun startIfNeeded() {
		if (!scheduler.isPaused && !scheduler.isQueueEmpty && !timer.isRunning()) {
			LOG.trace("Starting timer")
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
			val beginTime = System.currentTimeMillis()
			while (!scheduler.isQueueEmpty && System.currentTimeMillis() - beginTime < 20) {
				scheduler.execute()
			}
		} else {
			var count = 0
			while (count < 10 && scheduler.execute().recalculated) {
				count++
			}
		}
	}

	/** ---- [TimedSchedulerTask] */

	private fun calculateTimerInterval(): Int {
		val interval = max(1.0, SLOWDOWN_FACTOR * (SystemSpeed.MAX_SPEED - currentSystemSpeedCategory.systemSpeed.speed)).toInt()
		LOG.debug("interval = $interval")
		return interval
	}

	private fun adaptToSystemSpeed() {
		timer.interval = calculateTimerInterval()
	}
}