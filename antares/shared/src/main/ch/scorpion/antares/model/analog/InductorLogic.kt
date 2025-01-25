package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.jabbah.base.logger
import kotlin.math.abs

/**
 * Contains the pure inductor logic to be used in various [AnalogVertices][AnalogVertice].
 */
class InductorLogic {
    companion object {
        private val LOG by logger(InductorLogic::class)

        /** The default inductance for new [Inductor]s (in Henry). */
        const val DEF_INDUCTANCE = 0.2

        const val DEF_TRAPEZOIDAL = true

        /** The minimum voltage difference between the two [AnalogPort] for which recalculation is done.*/
        private const val VOLTAGE_LIMIT = AnalogSignal.VOLTAGE_SIGMA
    }

    /** The inductance of this [InductorLogic] in Henry.*/
    var inductance: Double = 0.0
        private set

    private var current: Double = 0.0

    private var isTrapezoidal: Boolean = DEF_TRAPEZOIDAL

    private var resistance: Double = 0.0

    private var curSourceValue: Double = 0.0

    private val nodes = IntArray(2)

    fun setup(inductance: Double, current: Double, isTrapezoidal: Boolean) {
        this.inductance = inductance
        this.current = current
        this.isTrapezoidal = isTrapezoidal
    }

    fun reset() {
        current = 0.0
    }

    fun stamp(analysis: AnalogCircuitAnalysis, n0: Int, n1: Int) {
        nodes[0] = n0
        nodes[1] = n1
        resistance = if (isTrapezoidal) {
            2.0 * inductance / analysis.timeStep
        } else {
            inductance / analysis.timeStep
        }
        analysis.stampResistor(nodes[0], nodes[1], resistance)
        analysis.stampRightSide(nodes[0])
        analysis.stampRightSide(nodes[1])
    }

    fun startIteration(voltDiff: Double) {
        curSourceValue = if (isTrapezoidal) {
            voltDiff / resistance + current
        } else {
            current
        }
    }

    fun calculateCurrent(voltDiff: Double): Double {
        if (resistance > 0.0) {
            current = voltDiff / resistance + curSourceValue
        }
        return current
    }

    fun doStepRequiresRecalculation(voltDiff: Double, analysis: AnalogCircuitAnalysis): Boolean {
        if (LOG.isTraceEnabled()) {
            LOG.trace("voltDiff = $voltDiff")
        }

        analysis.stampCurrentSource(nodes[0], nodes[1], curSourceValue)

        if (abs(voltDiff) >= VOLTAGE_LIMIT) {
            return true
        }

        return false
    }
}