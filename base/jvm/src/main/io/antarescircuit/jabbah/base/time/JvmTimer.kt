package io.antarescircuit.jabbah.base.time

import io.antarescircuit.jabbah.base.event.ActionEvent
import java.util.TimerTask

/** A real-time timer for JVM applications without a Swing event loop. */
class JvmTimer : Timer {

	private var initialized = false
	private var delay = 0
	private var repeats = false
	private lateinit var handler: TimerHandler
	private var timer: java.util.Timer? = null

	override var interval: Int
		get() = synchronized(this) {
			check(initialized) { "not yet initialized" }
			delay
		}
		set(value) = synchronized(this) {
			require(value >= 0) { "negative interval" }
			delay = value
			if (timer != null) {
				stop()
				start()
			}
		}

	@Synchronized
	override fun initialize(interval: Int, repeats: Boolean, handler: TimerHandler): Timer {
		check(!initialized) { "already initialized" }
		require(interval >= 0) { "negative interval" }
		delay = interval
		this.repeats = repeats
		this.handler = handler
		initialized = true
		return this
	}

	@Synchronized
	override fun start() {
		check(initialized) { "not yet initialized" }
		if (timer != null) return
		val scheduledTimer = java.util.Timer("jabbah-timer", true)
		timer = scheduledTimer
		val task = object : TimerTask() {
			override fun run(): Unit = synchronized(this@JvmTimer) {
				if (timer !== scheduledTimer) return
				if (!repeats) stop()
				try {
					handler(ActionEvent(null, this@JvmTimer, 0, "", java.lang.System.currentTimeMillis()))
				} catch (error: Throwable) {
					stop()
					throw error
				}
			}
		}
		if (repeats) {
			scheduledTimer.schedule(task, delay.toLong(), maxOf(1L, delay.toLong()))
		} else {
			scheduledTimer.schedule(task, delay.toLong())
		}
	}

	@Synchronized
	override fun stop() {
		check(initialized) { "not yet initialized" }
		timer?.cancel()
		timer = null
	}

	@Synchronized
	override fun isRunning(): Boolean = timer != null
}
