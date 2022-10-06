package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

data class TunnelName(val name: String) {
	override fun toString(): String = name
}

/**
 * A [Tunnel] forwards a signal to other [Tunnel]s with the same name without the
 * need to explicitly connect them by a [Net].
 * [Tunnel]s with the same name are only connected within the same [Graph].
 * The owning [Graph] will be informed by [stateChanged()], which gets already
 * called by [AbstractVertice.inputChanged].
 */
class Tunnel(
	name: String? = null
) : CalculatingVertice(CALCULATOR), NetCombiner {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Tunnel"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Tunnel> {
			override fun calculate(vertice: Tunnel, data: GraphActorData, signalHandler: SignalHandler) {
				(vertice.getPort<DigitalSignal>() as DigitalPort).isOutputDominant = true
				if (data.changedPort === vertice.getPort<DigitalSignal>(1)) {
					vertice.getOutput<DigitalSignal>(2).setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
				} else if (data.changedPort === vertice.getPort<DigitalSignal>(2)) {
					vertice.getOutput<DigitalSignal>(1).setOutgoingSignalBuffered(data.getSignal(2), signalHandler)
				}
			}
		}
	}

	init {
		propagationDelay = 0
		this.name = name

		// The Port to which the user connects visible Nets (portId 1)
		addPort(DigitalPortImpl.createInOut())

		// The Port to which DigitalGraph connects the invisible Tunnel Nets (portId 2)
		addPort(DigitalPortImpl.createInOut())
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	var bitWidth: BitWidth
		get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
		set(newValue) {
			if (newValue != bitWidth) {
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = newValue
				stateChanged()
			}
		}

	var tunnelName: TunnelName?
		get() = name?.let { TunnelName(it) }
		set(value) {
			name = value?.let { it.name }
		}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
	}

	/** ---- [Actor] */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		val undefined = DigitalSignalFactory.allOf(bitWidth, Bit.Undefined)
		getOutput<DigitalSignal>(1).setOutgoingSignalBuffered(undefined, signalHandler)
		getOutput<DigitalSignal>(2).setOutgoingSignalBuffered(undefined, signalHandler)
	}

	/** ---- [NetCombiner] */

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = false

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> {
		val otherPort: OutputPort<DigitalSignal> = if (inputPort === getPort<DigitalSignal>(1)) {
			getOutput(2)
		} else {
			getOutput(1)
		}

		val result = CombinedNet.createFor(otherPort, signalHandler)
		result.forEach { it.replaceAccessPort(otherPort, outputPort as OutputPort<DigitalSignal>) }
		return result as Collection<CombinedNet<T>>
	}

	/** ---- [Tunnel] */

	private fun getIncomingSignal(): DigitalSignal = getInput<DigitalSignal>().getIncomingSignal()!!

	private fun getOutgoingSignal(): DigitalSignal = getOutput<DigitalSignal>().getOutgoingSignal()!!

	fun getInOrOutSignal(): DigitalSignal {
		if (getIncomingSignal().isAllOf(Bit.Undefined)) {
			return getOutgoingSignal()
		}
		return getIncomingSignal()
	}
}
