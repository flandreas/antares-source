package io.antarescircuit.jabbah.base.time

import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.event.PropertyChangeSupport
import io.antarescircuit.jabbah.base.logger

/** A service that provides the current time.*/
interface TimeService {

	/** Returns the current time in milliseconds.*/
	fun nowMillis(): Long

	/** Returns the current time in nanoseconds.*/
	fun nowNanos(): Long
}

/**
 * A [TimeService] that allows to control the flow of time programmatically.
 * Informs registered [PropertyChangeListener]s whenever the current time has changed.
 */
class ControlledTimeService : TimeService {

	companion object {
		private val LOG by logger(ControlledTimeService::class)
		const val PROP_TIME = "time"
	}

	private var pcSupport: PropertyChangeSupport<Long> = PropertyChangeSupport(this)

	/** Holds the current time in nanoseconds.*/
	private var _timeNanos: Long = 0

	/** ---- [TimeService] interface */

	override fun nowMillis(): Long = _timeNanos / 1_000_000

	override fun nowNanos(): Long = _timeNanos

	/** ---- [ControlledTimeService] */

	fun reset() {
		val oldValue = _timeNanos
		_timeNanos = 0
		pcSupport.fire(PROP_TIME, oldValue, _timeNanos)
	}

	fun setTimeMillis(timeMillis: Long) {
		setTimeNanos(1_000_000 * timeMillis)
	}

	fun setTimeNanos(timeNanos: Long) {
		if (timeNanos < _timeNanos) {
			throw IllegalArgumentException("Time can only flow forward")
		}
		val oldValue = _timeNanos
		_timeNanos = timeNanos
		pcSupport.fire(PROP_TIME, oldValue, _timeNanos)

		LOG.trace("Fired TimeEvent at $_timeNanos ns")
	}

	fun addPropertyChangeListener(l: PropertyChangeListener<Long>) = pcSupport.add(l)

	fun removePropertyChangeListener(l: PropertyChangeListener<Long>) = pcSupport.remove(l)
}
