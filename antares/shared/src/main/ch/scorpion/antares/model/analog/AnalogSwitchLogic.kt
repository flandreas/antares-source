package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement

class AnalogSwitchLogic(
    private val analogElem: AnalogElement,
    private val isOn: () -> Boolean
) {
    companion object {
        private const val ON_RESISTANCE = 0.0
        private const val OFF_RESISTANCE = 100_000_000.0
    }

    val resistance: Double get() = if (isOn()) ON_RESISTANCE else OFF_RESISTANCE

    val voltageSourceCount: Int get() = if (isOn()) 1 else 0

    fun stamp(analysis: AnalogCircuitAnalysis) {
        if (isOn()) {
            analysis.stampVoltageSource(analogElem.getNode(0), analogElem.getNode(1), analogElem.getVoltageSource(0), 0.0)
        }
    }
}