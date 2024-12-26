package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import kotlin.math.abs

/**
 * Contains the pure inductor logic to be used in various [AnalogVertices][AnalogVertice].
 */
class InductorLogic(
    private val analogElem: AnalogElement
) {
    companion object {
        private val LOG by logger(InductorLogic::class)

        /** The minimum voltage difference between the two [AnalogPort] for which recalculation is done.*/
        private const val VOLTAGE_LIMIT = 0.01
    }

    /** The inductance of this [InductorLogic] in microhenry.*/
    var inductance: Double = 0.0
        private set

    private var current: Double = 0.0

    private var isTrapezoidal: Boolean = false

    private var resistance: Double = 0.0

    private var curSourceValue: Double = 0.0

    private val voltDiff: Double get() = analogElem.getNodeVoltage(0) - analogElem.getNodeVoltage(1)

    fun setup(inductance: Double, current: Double, isTrapezoidal: Boolean) {
        this.inductance = inductance
        this.current = current
        this.isTrapezoidal = isTrapezoidal
    }

    fun reset() {
        current = 0.0
    }

    fun stamp(analysis: AnalogCircuitAnalysis) {
        resistance = if (isTrapezoidal) {
            2.0 * inductance * 1e-6 / analysis.timeStep
        } else {
            inductance * 1e-6 / analysis.timeStep
        }
        analysis.stampResistor(analogElem.getNode(0), analogElem.getNode(1), resistance)
        analysis.stampRightSide(analogElem.getNode(0))
        analysis.stampRightSide(analogElem.getNode(1))
    }

    fun startIteration() {
        curSourceValue = if (isTrapezoidal) {
            voltDiff / resistance + analogElem.getInternalCurrent()
        } else {
            analogElem.getInternalCurrent()
        }
    }

    fun calculateCurrent() {
        if (resistance > 0.0) {
            analogElem.setInternalCurrent(0, voltDiff / resistance + curSourceValue)
        }
    }

    fun doStepRequiresRecalculation(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler): Boolean {
        if (LOG.isTraceEnabled()) {
            LOG.trace("voltDiff = $voltDiff")
        }

        analysis.stampCurrentSource(analogElem.getNode(0), analogElem.getNode(1), curSourceValue)

        if (abs(voltDiff) >= VOLTAGE_LIMIT) {
            return true
        }

        return false
    }
}