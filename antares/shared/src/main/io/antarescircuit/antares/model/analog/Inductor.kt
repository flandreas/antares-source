package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.AnalogGraphView
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

class Inductor(
    inductance: MagnitudeValue = InductorLogic.DEF_INDUCTANCE
) : AbstractAnalogTwoPortVertice<Inductor>(
    EmptyVerticeCalculator,
    "library.element.Inductor",
    AnalogElementMixin(true)
) {

    private val logic = InductorLogic()

    private val voltDiff: Double get() = analogElem.getNodeVoltage(0) - analogElem.getNodeVoltage(1)

    var inductance: MagnitudeValue = inductance
        set(value) {
            if (field != value) {
                field = value
                logic.setup(value.baseValue, getInternalCurrent(), InductorLogic.DEF_TRAPEZOIDAL)
                stateChanged(reason = MAIN_PROPERTY_STATE)
            }
        }

    var variable: Boolean = false

    init {
        logic.setup(this.inductance.baseValue, getInternalCurrent(), true)
    }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("inductance")) {
            // Backward compatability before MagnitudeValue was introduced
            inductance = MagnitudeValue(reader.readDouble("inductance"), Magnitude.One, SIUnit.Henry)
        } else if (reader.hasAttribute("inductance${MagnitudeValue.MAGNITUDE_VALUE_EXT}")) {
            inductance = MagnitudeValue.read("inductance", reader, SIUnit.Henry)
        }
        if (reader.hasAttribute("variable")) {
            variable = reader.readBoolean("variable")
        }
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        inductance.write("inductance", writer)
        if (variable) {
            writer.writeBoolean("variable", variable)
        }
    }

    /** ---- [AnalogElement] */

    override fun reset() {
        super.reset()
        analogElem.reset()
        logic.reset()
    }

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        logic.stamp(analysis, getNode(0), getNode(1))
    }

    override fun startIteration() {
        logic.startIteration(voltDiff)
    }

    override fun calculateCurrent() {
        analogElem.setInternalCurrent(0, logic.calculateCurrent(voltDiff))
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        if (logic.doStepRequiresRecalculation(voltDiff, analysis)) {
            requestAnalogReanalization(signalHandler)
        }
    }

    /** ---- [Inductor] */

    fun setState(inductance: MagnitudeValue, signalHandler: SignalHandler, graphView: AnalogGraphView) {
        this.inductance = inductance
        graphView.requireAnalysis()
        requestActingAfter(signalHandler, propagationDelay.value, createActorData(null))
    }
}