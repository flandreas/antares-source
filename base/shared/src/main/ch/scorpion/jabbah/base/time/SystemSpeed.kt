package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Represents the overall speed at which a system runs between 0 percent and 100 percent.
 */
class SystemSpeed(
	speed: Int = BaseModule.settings.getInt(SETTING_SPEED, DEFAULT_SPEED),
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {
		/** The name of the [Int] speed property in [Properties].*/
		private const val SETTING_SPEED = "jabbah.base.time.SystemSpeed"
		const val DEFAULT_SPEED: Int = 60
		const val MIN_SPEED: Int = 0
		const val MAX_SPEED: Int = 100
	}

	private var _speed = speed

	var speed: Int
		get() = _speed
		set(value) {
			require(speed in MIN_SPEED..MAX_SPEED) { "SystemSpeed must be between $MIN_SPEED and $MAX_SPEED" }
			val event = SystemSpeedEvent(this, _speed, value)
			if (systemSpeedConfirmer == null) {
				commitSpeed(event)
			} else {
				systemSpeedConfirmer!!.confirmSystemSpeedEvent(event)
			}
		}

	private fun commitSpeed(event: SystemSpeedEvent) {
		if (event.newSpeed != _speed) {
			BaseModule.settings.set(SETTING_SPEED, event.newSpeed)
			_speed = event.newSpeed
			eventBus.post(event)

		}
	}

	val isMaximum: Boolean get() = speed == MAX_SPEED

	var isPaused: Boolean = false
		private set

	/**
	 * An object that wants to explicitly confirm [SystemSpeed] changes before they are committed.
	 * In the future, there might be many of them. For the moment, only one should suffice.
	 */
	var systemSpeedConfirmer: SystemSpeedConfirmer? = null

	fun pause() {
		if (!isPaused) {
			isPaused = true
			eventBus.post(SystemSpeedPauseEvent(this, isPaused))
		}
	}

	fun resume() {
		if (isPaused) {
			isPaused = false
			eventBus.post(SystemSpeedPauseEvent(this, isPaused))
		}
	}

	/**
	 * Called by [SystemSpeedConfirmer] to confirm that a requested [SystemSpeedEvent] can be committed.
	 */
	fun commit(event: SystemSpeedEvent) {
		// TODO
	}
}

/** Posted by [SystemSpeed] when the current value of [SystemSpeed] has changed. */
data class SystemSpeedEvent(val source: SystemSpeed, val oldSpeed: Int, val newSpeed: Int)

data class SystemSpeedPauseEvent(val source: SystemSpeed, val isPaused: Boolean)