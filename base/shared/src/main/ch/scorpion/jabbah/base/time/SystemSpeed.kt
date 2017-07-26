package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Represents the overall speed at which a system runs between 0 percent and 100 percent.
 */
class SystemSpeed(
    private val properties: Properties = BaseModule.properties,
    private val eventBus: EventBus = BaseModule.eventBus
) {

    companion object {
        /** The name of the [Int] speed property in [Properties].*/
        private val PROP_SPEED = "jabbah.base.time.SystemSpeed"
        val DEFAULT_SPEED: Int = 80
        val MIN_SPEED: Int = 0
        val MAX_SPEED: Int = 100
    }

    var speed = properties.getInt(PROP_SPEED, DEFAULT_SPEED)
        set(value) {
            checkArgument(speed in 0..100, "SystemSpeed must be between 0 and 100")
            properties.set(PROP_SPEED, value)
            val oldSpeed = field
            field = value
            eventBus.post(SystemSpeedEvent(oldSpeed, field))
        }
}

/** Posted by [SystemSpeed] when the current speed has changed. */
data class SystemSpeedEvent(val oldSpeed: Int, val newSpeed: Int)