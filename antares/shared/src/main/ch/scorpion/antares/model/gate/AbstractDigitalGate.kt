package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

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
	    const val DEFAULT_PROPAGATION_DELAY = 20L
        val DEF_MIN_INPUT_COUNT = InputCount.TWO
        val DEF_MAX_INPUT_COUNT = InputCount.EIGHT
    }

	val chosenInputCount: InputCount get() = InputCount.of(inputCount)

    init {
        propagationDelay = DEFAULT_PROPAGATION_DELAY
	    setupInputCount(inputCount)
    }

	private fun setupInputCount(inputCount: InputCount) {
		clearPorts()
		for (i in 1..inputCount.count) {
			addPort(createInputPort())
		}
		addPort(createOutputPort())
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
	    setupInputCount(InputCount.of(reader.readInt("inputCount")))
        if (reader.hasAttribute("outputName")) {
            getOutput<DigitalSignal>().name = reader.readString("outputName")
        }
    }

    /** ---- [Actor] interface */

    override fun executionStart(signalHandler: SignalHandler) {
        super.executionStart(signalHandler)
        requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
    }

    /** ---- [AbstractDigitalGate] */

    open val minInputCount: InputCount get() = DEF_MIN_INPUT_COUNT

    open val maxInputCount: InputCount get() = DEF_MAX_INPUT_COUNT

	open fun createInputPort(): InputPort<DigitalSignal> = DigitalPortImpl.createInput()

    /**
     * Called by setter [chosenInputCount] which establishes the required [InputPort] and a single [OutputPort],
     * which is created by this method.
     */
    protected open fun createOutputPort(): OutputPort<DigitalSignal> = DigitalPortImpl.createOutput()
}