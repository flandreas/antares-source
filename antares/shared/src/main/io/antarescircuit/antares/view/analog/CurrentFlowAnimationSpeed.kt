package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.base.module.BaseModule

data class CurrentFlowAnimationSpeedEvent(val newSpeed: Int)

/**
 * Defines the speed factor used for animating the flow of electrical current in analog
 * circuits. Intended to be changed interactively by the user during simulation.
 */
object CurrentFlowAnimationSpeed {

    const val PROP_SPEED = "antares.view.analog.currentSpeed"

    const val DEF_SPEED = 15
    const val MIN_SPEED = 1
    const val MAX_SPEED = 40

    var speed: Int = BaseModule.properties.getInt(PROP_SPEED)
        set(value) {
            if (field != value) {
                field = value
                BaseModule.properties.customize(PROP_SPEED, value)
                BaseModule.eventBus.post(CurrentFlowAnimationSpeedEvent(value))
            }
        }
}