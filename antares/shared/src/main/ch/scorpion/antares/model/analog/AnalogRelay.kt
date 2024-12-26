package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class AnalogRelay(
    inductance: Double = InductorLogic.DEF_INDUCTANCE
) : AbstractAnalogVertice<AnalogRelay>(
    EmptyVerticeCalculator,
    "library.element.AnalogRelay",
    AnalogElementMixin(true, 5)
) {
    // TODO: Dynamic
    private var isOn: Boolean = false

    private val inductorLogic = InductorLogic(this, 0)

    private val switchLogic = AnalogSwitchLogic(this, 2, ::isOn)

    /** The inductance of this [AnalogRelay] in microhenry.*/
    var inductance: Double
        get() = inductorLogic.inductance
        set(value) {
            if (inductorLogic.inductance != value) {
                inductorLogic.setup(value, 0.0, InductorLogic.DEF_TRAPEZOIDAL)
            }
        }

    init {
        (1..4).forEach { _ -> addPort(AnalogPort()) }
        inductorLogic.setup(inductance, 0.0, InductorLogic.DEF_TRAPEZOIDAL)
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
        inductorLogic.reset()
    }

    override val voltageSourceCount: Int get() = switchLogic.voltageSourceCount

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        inductorLogic.stamp(analysis)
        switchLogic.stamp(analysis)
    }

    override fun startIteration() {
        inductorLogic.startIteration()
    }

    override fun calculateCurrent() {
        inductorLogic.calculateCurrent()
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        if (inductorLogic.doStepRequiresRecalculation(analysis, signalHandler)) {
            requestAnalogGraphRecalculation(signalHandler)
        }
    }
}