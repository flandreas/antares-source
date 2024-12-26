package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class Inductor(
    inductance: Double = DEF_INDUCTANCE
) : AbstractAnalogTwoPortVertice<Inductor>(
    EmptyVerticeCalculator,
    "library.element.Inductor",
    AnalogElementMixin(true)
) {

    companion object {
        /** The default inductance for new [Inductor]s (in microhenry). */
        private const val DEF_INDUCTANCE = 10.0
    }

    private val logic = InductorLogic(this)

    /** The inductance of this [Inductor] in microhenry.*/
    var inductance: Double
        get() = logic.inductance
        set(value) {
            if (logic.inductance != value) {
                logic.setup(value, getInternalCurrent(), true)
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
        logic.stamp(analysis)
    }

    override fun startIteration() {
        logic.startIteration()
    }

    override fun calculateCurrent() {
        logic.calculateCurrent()
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        if (logic.doStepRequiresRecalculation(analysis, signalHandler)) {
            requestAnalogGraphRecalculation(signalHandler)
        }
    }
}