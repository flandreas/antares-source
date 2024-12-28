package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement

class AnalogDoubleThrowSwitchLogic(
    private val analogElem: AnalogElement,
    private val postBase: Int,
    private val isOn: () -> Boolean
) {
    val voltageSourceCount: Int get() = 1

    fun stamp(analysis: AnalogCircuitAnalysis) {
        analysis.stampVoltageSource(
            analogElem.getNode(postBase),
            if (isOn()) analogElem.getNode(postBase + 1) else analogElem.getNode(postBase + 2),
            analogElem.getVoltageSource(postBase),
            0.0)
    }
}