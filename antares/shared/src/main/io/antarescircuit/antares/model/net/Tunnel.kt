package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.vertice.AdjustableBitWidth
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.net.CombinedNet
import io.antarescircuit.jabbah.graph.model.net.NetCombiner
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Wraps [String] in order to allow configuring a special editor with a drop-down list of all
 * already existing [Tunnel] names.
 */
data class TunnelName(val name: String) {
	override fun toString(): String = name
}

/**
 * A [Tunnel] forwards a signal to other [Tunnel]s with the same name without the
 * need to explicitly connect them by a [Net].
 * [Tunnel]s can be either local or global. Local [Tunnel]s with the same name are only connected within the same [Graph].
 * Global [Tunnel]s communicate with all other [Tunnel]s with the same name in the entire simulation context,
 * i.e. with the main [DigitalGraph] and the [DigitalGraph]s in all subcircuits.
 */
class Tunnel(
	name: String? = null
) : CalculatingVertice(CALCULATOR), NetCombiner, AdjustableBitWidth {

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
		propagationDelay = LongValueImpl.ZERO
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
			name = value?.name
		}

	/**
	 * A global [Tunnel] can communicate not only with other [Tunnel]s of the same [Graph], but with all
	 * [Tunnel]s of the same [Library].
	 */
	var isGlobal: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	val visiblePort: DigitalPort get() = getPort<DigitalSignal>(1) as DigitalPort
	val invisiblePort: DigitalPort get() = getPort<DigitalSignal>(2) as DigitalPort

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		return true
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
		if (isGlobal) {
			writer.writeBoolean("global", isGlobal)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
		if (reader.hasAttribute("global")) {
			isGlobal = reader.readBoolean("global")
		}
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

	@Suppress("UNCHECKED_CAST")
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

	/** ---- [Vertice] */

	override val isFullyConnected: Boolean get() = visiblePort.isConnected

	override val hasUnconnectedInput: Boolean get() = !visiblePort.isConnected

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
