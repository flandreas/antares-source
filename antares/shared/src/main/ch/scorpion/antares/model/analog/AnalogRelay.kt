package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.abs

class AnalogRelay(
    inductance: Double = InductorLogic.DEF_INDUCTANCE
) : AbstractAnalogVertice<AnalogRelay>(
    EmptyVerticeCalculator,
    "library.element.AnalogRelay",
    AnalogElementMixin(true, 4)
) {
    companion object {
        private const val DEF_ON_CURRENT = 0.02
    }

    var isOn: Boolean = false
        private set

    private val inductorLogic = InductorLogic(this, 0)

    private val switchLogic = AnalogSwitchLogic(this, 2, ::isOn)

    private var coilCurrent: Double = 0.0

    /** The inductance of this [AnalogRelay] in microhenry.*/
    var inductance: Double
        get() = inductorLogic.inductance
        set(value) {
            if (inductorLogic.inductance != value) {
                inductorLogic.setup(value, 0.0, InductorLogic.DEF_TRAPEZOIDAL)
            }
        }

    /** The current (in A) through the inductor at which the [AnalogRelay] is switched on. */
    var onCurrent: Double = DEF_ON_CURRENT
        set(value) {
            require(value > 0) { Translations.getString("element.property.relay.onCurrentNotLargerThanZero.msg") }
            field = value
        }

    init {
        (1..4).forEach { _ -> addPort(AnalogPort()) }
        inductorLogic.setup(inductance, 0.0, InductorLogic.DEF_TRAPEZOIDAL)
    }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        inductance = reader.readString("inductance").toDouble()
        onCurrent = reader.readString("onCurrent").toDouble()
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("inductance", inductance.toString())
        writer.writeString("onCurrent", onCurrent.toString())
    }

    /** ---- [AnalogElement] */

    override fun reset() {
        super.reset()
        isOn = false
        coilCurrent = 0.0
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

        // Calculate switch on/off
        isOn = abs(coilCurrent) >= onCurrent
    }

    override fun calculateCurrent() {
        coilCurrent = inductorLogic.calculateCurrent()
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        if (inductorLogic.doStepRequiresRecalculation(analysis, signalHandler)) {
            requestAnalogGraphRecalculation(signalHandler)
        }
    }
}