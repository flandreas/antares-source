package io.antarescircuit.jabbah.execution.scheduler

/** Contains the logic for driving the execution processing of a [Scheduler].*/
interface SchedulerTask {

	/**
	 * The base translation key of the name of this [SchedulerTask].
	 * Allows the (developer) user to distinguishing different [SchedulerTask] implementations.
	 */
	val nameKey: String

	/** Binds this [SchedulerTask] with the [Scheduler] it drives. Used to break construction dependency loop. */
	fun bind(scheduler: Scheduler)

	fun startIfNeeded()

	fun stop()
}

abstract class AbstractSchedulerTask(override val nameKey: String) : SchedulerTask {

	private lateinit var scheduler: Scheduler

	override fun bind(scheduler: Scheduler) {
		this.scheduler = scheduler
	}
}