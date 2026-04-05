package io.antarescircuit.jabbah.base.time

import io.antarescircuit.jabbah.base.event.ActionEvent

/** A [Timer] whose timing events can be controlled by a [ControlledTimeService].*/
class ControlledTimer(val timeService: ControlledTimeService) : Timer {

	override var interval: Int = 0

	/** The interval between two timer ticks in nanoseconds.*/
	private val intervalNs: Int get() = interval * 1_000_000

	/** Determines whether this [Timer] is currently running.*/
	private var running: Boolean = false

	/** The time in nanoseconds at which the last timing events had been sent.*/
	private var lastTimerTime: Long = 0

	private var repeats: Boolean = true

	private var handler: ((ActionEvent) -> Unit)? = null

	init {
		timeService.addPropertyChangeListener { timeChanged() }
	}

	/** ---- [Timer] interface */

	override fun initialize(interval: Int, repeats: Boolean, handler: (ActionEvent) -> Unit): Timer {
		ensureUninitialized()
		this.interval = interval
		this.handler = handler
		this.repeats = repeats

		return this
	}

	override fun start() {
		ensureInitialized()
		if (running) {
			return
		}
		running = true
	}

	override fun stop() {
		ensureInitialized()
		if (!running) {
			return
		}
		running = false
	}

	override fun isRunning(): Boolean {
		return running
	}

	/** ---- [ControlledTimer] */

	private fun ensureUninitialized() {
		if (handler != null) {
			throw IllegalStateException("already initialized")
		}
	}

	private fun ensureInitialized() {
		if (handler == null) {
			throw IllegalStateException("not yet initialized")
		}
	}

	private fun timeChanged() {
		if (!running) {
			return
		}

		val now = timeService.nowNanos()
		if (now >= lastTimerTime + intervalNs) {
			lastTimerTime = now
			fireActionPerformed(now)
			if (!repeats) {
				stop()
			}
		}
	}

	private fun fireActionPerformed(now: Long) {
		handler!!(ActionEvent(event = this, source = this, modifiers = 0, action = "timer", time = now))
	}
}