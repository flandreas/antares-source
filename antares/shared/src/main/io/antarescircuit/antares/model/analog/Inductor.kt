package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class Inductor(
    inductance: Double = InductorLogic.DEF_INDUCTANCE
) : AbstractAnalogTwoPortVertice<Inductor>(
    EmptyVerticeCalculator,
    "library.element.Inductor",
    AnalogElementMixin(true)
) {

    private val logic = InductorLogic()

    private val voltDiff: Double get() = analogElem.getNodeVoltage(0) - analogElem.getNodeVoltage(1)

    /** The inductance of this [Inductor] in microhenry.*/
    var inductance: Double
        get() = logic.inductance
        set(value) {
            if (logic.inductance != value) {
                logic.setup(value, getInternalCurrent(), InductorLogic.DEF_TRAPEZOIDAL)
                stateChanged(reason = MAIN_PROPERTY_STATE)
            }
        }

    init {
        logic.setup(inductance, getInternalCurrent(), true)
    }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        inductance = reader.readString("inductance").toDouble()
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("inductance", inductance.toString())
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
}