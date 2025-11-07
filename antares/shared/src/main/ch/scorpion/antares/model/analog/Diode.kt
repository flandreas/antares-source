package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.base.math.near
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import kotlin.math.*

/**
 * Conducts electric current from anode to cathode, blocks current in the reverse directory.
 * [AnalogPort] 1 is the anode, [AnalogPort] 2 is the cathode.
 */
open class Diode(
    baseResourceKey: String = "library.element.AnalogDiode"
) : AbstractAnalogTwoPortVertice<Diode>(
    EmptyVerticeCalculator,
    baseResourceKey,
    AnalogElementMixin(true)
) {

    private val zVoltage = 0.0
    private val leakage = 1e-14
    private val fwDrop = 0.80
    private val vdCoeff = ln(1.0 / leakage + 1.0) / fwDrop
    private var zOffset = 0.0
    private val vt = 1 / vdCoeff
    private val vCrit = vt * ln(vt / (sqrt(2.0) * leakage))

    private var lastVoltDiff = 0.0

    /** ---- [AnalogElement] */

    override fun reset() {
        super.reset()
        lastVoltDiff = 0.0
    }

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        analysis.stampNonLinear(analogElem.getNode(0))
        analysis.stampNonLinear(analogElem.getNode(1))
    }

    override fun calculateCurrent()  {
        val voltDiff = getNodeVoltage(0) - getNodeVoltage(1)
        val current = if (voltDiff >= 0 || zVoltage.near(0.0)) {
            leakage * (exp(voltDiff * vdCoeff) - 1.0)
        } else {
            leakage * (exp(voltDiff * vdCoeff) - exp((-voltDiff - zOffset)) - 1)
        }
        setInternalCurrent(0, current)
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        var voltDiff = getNodeVoltage(0) - getNodeVoltage(1)
        if (abs(voltDiff - lastVoltDiff) > 0.01) {
            analysis.converged = false
        }

        voltDiff = limitStep(analysis, voltDiff, lastVoltDiff)
        lastVoltDiff = voltDiff

        if (voltDiff >= 0.0 || zVoltage.near(0.0)) {
            var eval = exp(voltDiff * vdCoeff)
            if (voltDiff < 0) {
                eval = 1.0
            }
            val geq = vdCoeff * leakage * eval
            val nc = (eval -1) * leakage -geq * voltDiff
            analysis.stampConductance(analogElem.getNode(0), analogElem.getNode(1), geq)
            analysis.stampCurrentSource(analogElem.getNode(0), analogElem.getNode(1), nc)
        } else {
            // Not yet used
        }
    }

    private fun limitStep(analysis: AnalogCircuitAnalysis, voltageNew: Double, vOld: Double): Double {
        var vNew = voltageNew

        if (vNew > vCrit && abs(vNew - vOld) > (vt + vt)) {
            if (vOld > 0) {
                val arg = 1.0 + (vNew - vOld) / vt
                if (arg > 0) {
                    vNew = vOld + vt * ln(arg)
                    val v0 = ln(1e-6 / leakage) * vt
                    vNew = max(v0, vNew)
                } else {
                    vNew = vCrit
                }
            } else {
                vNew = vt * ln(vNew / vt)
            }
            analysis.converged = false
        } else if (vNew < 0 && !zOffset.near(0.0)) {
            // Not yet used
        }

        return vNew
    }
}