package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement

/**
 * Contains the pure switch logic to be used in various [AnalogVertices][AnalogVertice].
 * @property postBase the index in [analogElem]'s posts where this [AnalogSwitchLogic]'s post start
 */
class AnalogSwitchLogic(
    private val analogElem: AnalogElement,
    private val postBase: Int,
    private val isOn: () -> Boolean
) {
    val voltageSourceCount: Int get() = if (isOn()) 1 else 0

    fun stamp(analysis: AnalogCircuitAnalysis) {
        if (isOn()) {
            analysis.stampVoltageSource(
                analogElem.getNode(postBase),
                analogElem.getNode(postBase + 1),
                analogElem.getVoltageSource(postBase),
                0.0
            )
        }
    }
}