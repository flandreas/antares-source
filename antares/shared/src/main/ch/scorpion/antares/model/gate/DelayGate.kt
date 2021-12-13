package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthExpression
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.StoringGraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Delays a signal change for a given time.
 */
class DelayGateCalculator : VerticeCalculator<DelayGate> {
    override fun calculate(vertice: DelayGate, data: GraphActorData, signalHandler: SignalHandler) {
        vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
    }
}

class DelayGate : CalculatingVertice(CALCULATOR) {

    companion object {
	    private const val BASE_RESOURCE_KEY = "library.element.Delay"
	    private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
	    private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

	    val CALCULATOR = DelayGateCalculator()
    }

    init {
        propagationDelay = AbstractDigitalGate.DEFAULT_PROPAGATION_DELAY
	    addPort(DigitalPortImpl.createInput())
	    addPort(DigitalPortImpl.createOutput())
    }

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	/** The delay in nanoseconds.*/
    var delay: Long
        get() = propagationDelay
        set(value) {
            propagationDelay = value
        }

	var bitWidth: BitWidth
		get() = (getInput<DigitalSignal>() as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				(getInput<DigitalSignal>() as DigitalPort).bitWidth = value
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	override fun graphParamsChanged(graph: Graph) {
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
	}

	override fun createActorData(inputPort: InputPort<*>?): GraphActorData =
		StoringGraphActorData(inputPort, getInput<DigitalSignal>().getIncomingSignal(), true)

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeLong("delay", delay)
	    bitWidth.write("bitWidth", writer)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        delay = reader.readLong("delay")
	    if (reader.hasAttribute("bitWidth")) {
		    // Backward compatibility: Older version didn't have a BitWidth property
		    bitWidth = BitWidth.read("bitWidth", reader)
	    }
    }
}
