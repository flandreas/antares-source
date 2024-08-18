package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
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

        private const val CURRENT_LIMIT = 0.00001
    }

    private val isTrapezoidal = true

    /** The capacitance of this [Capacitor] in microfarad.*/
    var capacitance: Double = capacitance
        set(value) {
            if (field != value) {
                field = value
                stateChanged(reason = MAIN_PROPERTY_STATE)
            }
        }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        capacitance = reader.readDouble("capacitance")
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeDouble("capacitance", capacitance)
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
            analysis.timeStep / (2 * capacitance * 1e-6)
        } else {
            analysis.timeStep / (capacitance * 1e-6)
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