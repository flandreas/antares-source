package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.base.logger

/**
 * A digital gate is a [Vertice] that performs a basic logical operation on [DigitalSignal]s, whose number
 * of [InputPort]s can be chosen by the user up to a certain limit.
 */
abstract class AbstractDigitalGate(
    calculator: VerticeCalculator<*>,
    inputCount: InputCount
) : CalculatingVertice(calculator) {

    companion object {
        val LOG by logger(AbstractDigitalGate::class)
        val DEF_MIN_INPUT_COUNT = InputCount.TWO
        val DEF_MAX_INPUT_COUNT = InputCount.EIGHT
    }

    var chosenInputCount: InputCount = inputCount
        set(value) {
            checkArgument(value.count >= minInputCount.count, "InputCount must not be smaller than mininum ${minInputCount.count}")
            checkArgument(value.count <= maxInputCount.count, "InputCount must not be larger than maximum ${maxInputCount.count}")
            field = value
            clearPorts()
            for (i in 1..field.count) {
                addPort(DigitalPortImpl.createInput())
            }
            addPort(createOutputPort())
        }

    init {
        propagationDelay = 20
        chosenInputCount = inputCount
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeInt("inputCount", chosenInputCount.count)
        if (StringUtils.isNotEmpty(getOutput<DigitalSignal>().name)) {
            writer.writeString("outputName", getOutput<DigitalSignal>().name!!)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        chosenInputCount = InputCount.of(reader.readInt("inputCount"))
        if (reader.hasAttribute("outputName")) {
            getOutput<DigitalSignal>().name = reader.readString("outputName")
        }
    }

    /** ---- [Actor] interface */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
    }

    /** ---- [AbstractDigitalGate] */

    open val minInputCount: InputCount get() = DEF_MIN_INPUT_COUNT

    open val maxInputCount: InputCount get() = DEF_MAX_INPUT_COUNT

    /**
     * Called by setter [chosenInputCount] which establishs the required [InputPort] and a single [OutputPort],
     * which is created by this method.
     */
    protected open fun createOutputPort(): OutputPort<DigitalSignal> {
        return DigitalPortImpl.createOutput()
    }
}