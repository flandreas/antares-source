package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.abs

class Inductor(
    inductance: Double = DEF_INDUCTANCE
) : AbstractAnalogTwoPortVertice<Inductor>(
    EmptyVerticeCalculator,
    "library.element.Inductor",
    AnalogElementMixin(true)
) {

    companion object {
        private val LOG by logger(Inductor::class)

        /** The default inductance for new [Inductor]s (in microhenry)*/
        private const val DEF_INDUCTANCE = 10.0

        private const val VOLTAGE_LIMIT = 0.01
    }

    private val isTrapezoidal = true

    private var resistance: Double = 0.0

    private var curSourceValue: Double = 0.0

    /** The inductance of this [Inductor] in microhenry.*/
    var inductance: Double = inductance
        set(value) {
            if (field != value) {
                field = value
                stateChanged(reason = MAIN_PROPERTY_STATE)
            }
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

    private val voltDiff: Double get() = getNodeVoltage(0) - getNodeVoltage(1)

    override fun reset() {
        super.reset()
        analogElem.reset()
        curSourceValue = 0.0
    }

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        resistance = if (isTrapezoidal) {
            2.0 * inductance * 1e-6 / analysis.timeStep
        } else {
            inductance * 1e-6 / analysis.timeStep
        }
        analysis.stampResistor(analogElem.nodes[0], analogElem.nodes[1], resistance)
        analysis.stampRightSide(analogElem.nodes[0])
        analysis.stampRightSide(analogElem.nodes[1])
    }

    override fun startIteration() {
        curSourceValue = if (isTrapezoidal) {
            voltDiff / resistance + getInternalCurrent()
        } else {
            getInternalCurrent()
        }
    }

    override fun calculateCurrent() {
        if (resistance > 0.0) {
            setInternalCurrent(0, voltDiff / resistance + curSourceValue)
        }
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("voltDiff = $voltDiff")
        }

        analysis.stampCurrentSource(analogElem.nodes[0], analogElem.nodes[1], curSourceValue)

        if (abs(voltDiff) >= VOLTAGE_LIMIT) {
            requestAnalogGraphRecalculation(signalHandler)
        }
    }
}