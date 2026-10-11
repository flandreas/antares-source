package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.math.abs
import kotlin.random.Random

/** Operational amplifier.*/
class OpAmp(
    maxOutput: MagnitudeValue = DEF_MAX_OUTPUT,
    minOutput: MagnitudeValue = DEF_MIN_OUTPUT
) : AbstractAnalogVertice<OpAmp>(EmptyVerticeCalculator, "library.element.OpAmp", AnalogElementMixin(postCount = 3, isNonLinear = true)) {

    companion object {
        private val DEF_MAX_OUTPUT = MagnitudeValue(15.0, Magnitude.One, SIUnit.Volt)
        private val DEF_MIN_OUTPUT = MagnitudeValue(-15.0, Magnitude.One, SIUnit.Volt)

        private const val GAIN = 1_000.0
    }

    private var lastVd = 0.0

    var maxOutput: MagnitudeValue = maxOutput

    var minOutput: MagnitudeValue = minOutput

    val invertingPort: AnalogPort get() = getPort<AnalogSignal>(1) as AnalogPort

    val nonInvertingPort: AnalogPort get() = getPort<AnalogSignal>(2) as AnalogPort

    init {
        addPort(AnalogPort(name = "-"))
        addPort(AnalogPort(name = "+"))
        addPort(AnalogPort())
    }

    /** ---- [AbstractAnalogVertice] */

    override val voltageSourceCount: Int get() = 1

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        val vn = analysis.nodeList.size + analogElem.voltageSource
        analysis.stampNonLinear(vn)
        analysis.stampMatrix(getNode(2), vn, 1.0)
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        val vd = getNodeVoltage(1) - getNodeVoltage(0)
        if (abs(lastVd - vd) > 0.1) {
            analysis.converged = false
        } else if (getNodeVoltage(2) > maxOutput.baseValue + 0.1 || getNodeVoltage(2) < minOutput.baseValue - 0.1) {
            analysis.converged = false
        }

        var x = 0.0
        val vn = analysis.nodeList.size + analogElem.voltageSource
        var dx = 0.0
        if (vd >= maxOutput.baseValue / GAIN && (lastVd >= 0 || Random.nextInt(4) == 1)) {
            dx = 1E-4
            x = maxOutput.baseValue - dx * maxOutput.baseValue / GAIN
        } else if (vd <= minOutput.baseValue / GAIN && (lastVd <= 0 || Random.nextInt(4) == 1)) {
            dx = 1E-4
            x = minOutput.baseValue - dx * minOutput.baseValue / GAIN
        } else {
            dx = GAIN
        }

        // Newton-Raphson
        analysis.stampMatrix(vn, getNode(0), dx)
        analysis.stampMatrix(vn, getNode(1), -dx)
        analysis.stampMatrix(vn, getNode(2), 1.0)
        analysis.stampRightSide(vn, x)

        lastVd = vd
    }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        maxOutput = MagnitudeValue.read("maxOutput", reader, SIUnit.Volt)
        minOutput = MagnitudeValue.read("minOutput", reader, SIUnit.Volt)
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        maxOutput.write("maxOutput", writer)
        minOutput.write("minOutput", writer)
    }
}