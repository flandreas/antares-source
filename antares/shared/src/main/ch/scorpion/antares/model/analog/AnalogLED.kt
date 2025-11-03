package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.output.LightEmitterModel
import ch.scorpion.jabbah.graph.model.GraphElementEvent

class AnalogLED : Diode("library.element.AnalogLED"), LightEmitterModel {

    companion object {

        /**
         * The [GraphElementEvent.reason] sent when this [AnalogLED]'s current has changed.
         * Used in application like LED views of this [AnalogLED] that need to update their color
         * depending on the current.
         */
        const val REASON_CURRENT = "AnalogLEDCurrent"
    }

    override fun setInternalCurrent(index: Int, current: Double) {
        super.setInternalCurrent(index, current)
        stateChanged(null, REASON_CURRENT)
    }
}