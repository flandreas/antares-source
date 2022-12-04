package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.ActionListener

typealias TimerHandler = (ActionEvent) -> Unit

/**
 * A timer abstraction to be used for implementing various types of timers for simulation processes,
 * such as real-time timers or test timers that allow precise control over time.
 *
 * A [Timer] repeatedly fires action events at the specified intervals.
 */
interface Timer {

	/** The time in milliseconds between two ticks.*/
	var interval: Int

	/**
	 * Initializes this [Timer] with an interval and the one and only [ActionListener] to which
	 * timing events are sent.
	 *
	 * @param interval the interval in milliseconds
	 * @param handler the handler of an [ActionEvent] to be called after every timing interval
	 */
	fun initialize(interval: Int, repeats: Boolean = true, handler: TimerHandler): Timer

	fun start()

	fun stop()

	/**
	 * Determines whether this [Timer] is currently running, i.e. whether it has been started and not yet stopped.
	 */
	fun isRunning(): Boolean
}
