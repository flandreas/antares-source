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

	var speed = speed
		set(value) {
			require(speed in 0..100) { "SystemSpeed must be between 0 and 100" }
			BaseModule.settings.set(SETTING_SPEED, value)
			val oldSpeed = field
			field = value
			eventBus.post(SystemSpeedEvent(this, oldSpeed, field))
		}

	val isMaximum: Boolean get() = speed == MAX_SPEED

	var isPaused: Boolean = false
		private set

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
}

/** Posted by [SystemSpeed] when the current value of [SystemSpeed] has changed. */
data class SystemSpeedEvent(val source: SystemSpeed, val oldSpeed: Int, val newSpeed: Int)

data class SystemSpeedPauseEvent(val source: SystemSpeed, val isPaused: Boolean)