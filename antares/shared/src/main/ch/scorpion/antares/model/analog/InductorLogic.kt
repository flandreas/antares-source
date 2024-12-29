package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import kotlin.math.abs

/**
 * Contains the pure inductor logic to be used in various [AnalogVertices][AnalogVertice].
 * @property postBase the index in [analogElem]'s posts where this [InductorLogic]'s post start
 */
class InductorLogic(
    private val analogElem: AnalogElement,
    private val postBase: Int = 0
) {
    companion object {
        private val LOG by logger(InductorLogic::class)

        /** The default inductance for new [Inductor]s (in microhenry). */
        const val DEF_INDUCTANCE = 10.0

        const val DEF_TRAPEZOIDAL = true

        /** The minimum voltage difference between the two [AnalogPort] for which recalculation is done.*/
        private const val VOLTAGE_LIMIT = AnalogSignal.VOLTAGE_SIGMA
    }

    /** The inductance of this [InductorLogic] in microhenry.*/
    var inductance: Double = 0.0
        private set

    private var current: Double = 0.0

    private var isTrapezoidal: Boolean = DEF_TRAPEZOIDAL

    private var resistance: Double = 0.0

    private var curSourceValue: Double = 0.0

    private val voltDiff: Double get() = analogElem.getNodeVoltage(postBase) - analogElem.getNodeVoltage(postBase + 1)

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
        analysis.stampResistor(analogElem.getNode(postBase), analogElem.getNode(postBase + 1), resistance)
        analysis.stampRightSide(analogElem.getNode(postBase))
        analysis.stampRightSide(analogElem.getNode(postBase + 1))
    }

    fun startIteration() {
        curSourceValue = if (isTrapezoidal) {
            voltDiff / resistance + current
        } else {
            current
        }
    }

    fun calculateCurrent(): Double {
        if (resistance > 0.0) {
            current = voltDiff / resistance + curSourceValue
        }
        return current
    }

    fun doStepRequiresRecalculation(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler): Boolean {
        if (LOG.isTraceEnabled()) {
            LOG.trace("voltDiff = $voltDiff")
        }

        analysis.stampCurrentSource(analogElem.getNode(postBase), analogElem.getNode(postBase + 1), curSourceValue)

        if (abs(voltDiff) >= VOLTAGE_LIMIT) {
            return true
        }

        return false
    }
}