package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.output.LightEmitterModel
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.element.AbstractGraphElement

class AnalogLED : Diode("library.element.AnalogLED"), LightEmitterModel {

    companion object {

        /**
         * The [GraphElementEvent.reason] sent when this [AnalogLED]'s current has changed.
         * Used in application like LED views of this [AnalogLED] that need to update their color
         * depending on the current.
         */
        const val REASON_CURRENT = "AnalogLEDCurrent"
    }

    /** ---- [AnalogElement] */

    override fun setInternalCurrent(index: Int, current: Double) {
        super.setInternalCurrent(index, current)
        stateChanged(null, REASON_CURRENT)
    }

    /** ---- [AbstractGraphElement] */

    override fun graphParamsChanged(graph: Graph) {
        super.graphParamsChanged(graph)
        stateChanged(null, LightEmitterModel.REASON_GRAPH_PARAM_CHANGED, graph)
    }
}