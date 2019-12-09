package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Represents the overall speed at which a system runs between 0 percent and 100 percent.
 */
class SystemSpeed(
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {
		/** The name of the [Int] speed property in [Properties].*/
		private const val SETTING_SPEED = "jabbah.base.time.SystemSpeed"
		const val DEFAULT_SPEED: Int = 60
		const val MIN_SPEED: Int = 0
		const val MAX_SPEED: Int = 100
	}

	var speed = BaseModule.settings.getInt(SETTING_SPEED, DEFAULT_SPEED)
		set(value) {
			checkArgument(speed in 0..100, "SystemSpeed must be between 0 and 100")
			BaseModule.settings.set(SETTING_SPEED, value)
			val oldSpeed = field
			field = value
			eventBus.post(SystemSpeedEvent(oldSpeed, field))
		}

	val isMaximum: Boolean get() = speed == MAX_SPEED
}

/** Posted by [SystemSpeed] when the current speed has changed. */
data class SystemSpeedEvent(val oldSpeed: Int, val newSpeed: Int)