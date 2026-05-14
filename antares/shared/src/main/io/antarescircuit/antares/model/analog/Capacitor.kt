package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElement
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

class Capacitor(
    capacitance: Double = DEF_CAPACITANCE,
) : AbstractAnalogTwoPortVertice<Capacitor>(
    EmptyVerticeCalculator,
    "library.element.Capacitor",
    AnalogElementMixin(true)
) {

    companion object {

        /** The default capacitance for new [Capacitors][Capacitor] (in microfarad).*/
        private const val DEF_CAPACITANCE = 200.0

        private const val INIT_VOLT_DIFF = 1e-3

        private const val CURRENT_LIMIT = 0.0001
    }

    private val isTrapezoidal = true

    var capacitance: MagnitudeValue = MagnitudeValue(capacitance, Magnitude.Micro, SIUnit.Farad)
        set (value) {
            if (field != value) {
                field = value
                stateChanged(reason = MAIN_PROPERTY_STATE)
            }
        }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("capacitance")) {
            // Backward compatability before MagnitudeValue was introduced
            capacitance = MagnitudeValue(reader.readDouble("capacitance"), Magnitude.Micro, SIUnit.Farad)
        } else if (reader.hasAttribute("capacitance${MagnitudeValue.MAGNITUDE_VALUE_EXT}")) {
            capacitance = MagnitudeValue.read("capacitance", reader, SIUnit.Farad)
        }
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        capacitance.write("capacitance", writer)
    }

    /** ---- [AnalogElement] */

    private var voltDiff: Double = INIT_VOLT_DIFF

    private var resistance: Double = 0.0

    private var curSourceValue: Double = 0.0

    override fun reset() {
        super.reset()
        this.voltDiff = INIT_VOLT_DIFF
        curSourceValue = 0.0
    }

    override fun setNodeVoltage(postId: Int, voltage: Double) {
        super.setNodeVoltage(postId, voltage)
        voltDiff = getNodeVoltage(0) - getNodeVoltage(1)
    }

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        resistance = if (isTrapezoidal) {
            analysis.timeStep / (2 * capacitance.baseValue)
        } else {
            analysis.timeStep / (capacitance.baseValue)
        }
        analysis.stampResistor(analogElem.nodes[0], analogElem.nodes[1], resistance)
        analysis.stampRightSide(analogElem.nodes[0])
        analysis.stampRightSide(analogElem.nodes[1])
    }

    override fun startIteration() {
        curSourceValue = if (isTrapezoidal) {
            -voltDiff / resistance - getInternalCurrent()
        } else {
            -voltDiff / resistance
        }
    }

    override fun calculateCurrent() {
        val voltDiff = getNodeVoltage(0) - getNodeVoltage(1)
        if (resistance > 0.0) {
            setInternalCurrent(0, voltDiff / resistance + curSourceValue)
        }
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        analysis.stampCurrentSource(analogElem.nodes[0], analogElem.nodes[1], curSourceValue)

        if (abs(getInternalCurrent()) >= CURRENT_LIMIT) {
            requestAnalogGraphRecalculation(signalHandler)
        }
    }
}