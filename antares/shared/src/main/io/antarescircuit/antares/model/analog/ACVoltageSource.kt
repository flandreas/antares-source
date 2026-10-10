package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.math.PI
import kotlin.math.sin

/**
 * A 2 [Port] source of alternativ electrical voltage.
 */
class ACVoltageSource(
    maxVoltage: MagnitudeValue = DEF_MAX_VOLTAGE,
    frequency: MagnitudeValue = DEF_FREQUENCY
) : AbstractAnalogTwoPortVertice<ACVoltageSource>(
    EmptyVerticeCalculator,
    "library.element.ACVoltageSource"
) {

    companion object {
        private val DEF_MAX_VOLTAGE = MagnitudeValue(9.0, Magnitude.One, SIUnit.Volt)
        private val DEF_FREQUENCY = MagnitudeValue(50.0, Magnitude.Kilo, SIUnit.Hertz)
    }

    var maxVoltage: MagnitudeValue = maxVoltage

    var frequency: MagnitudeValue = frequency
        set (value) {
            if (field != value) {
                field = value
                stateChanged(reason = MAIN_PROPERTY_STATE)
            }
        }

    /** ---- [AnalogElement] */

    override val voltageSourceCount: Int get() = 1

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        analysis.stampVoltageSource(analogElem.nodes[0], analogElem.nodes[1], analogElem.voltageSource)
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        analysis.updateVoltageSource(analogElem.voltageSource, getVoltage(signalHandler))
        requestAnalogGraphRecalculation(signalHandler)
    }

    private fun getVoltage(signalHandler: SignalHandler): Double {
        // executionTime is in nanoseconds, frequency is in seconds
        val w = 2 * PI * (signalHandler.executionTime / 1_000_000_000.0) * frequency.baseValue
        return sin(w) * maxVoltage.value
    }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        maxVoltage = MagnitudeValue.read("maxVoltage", reader, SIUnit.Volt)
        frequency = MagnitudeValue.read("frequency", reader, SIUnit.Hertz)
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        maxVoltage.write("maxVoltage", writer)
        frequency.write("frequency", writer)
    }
}