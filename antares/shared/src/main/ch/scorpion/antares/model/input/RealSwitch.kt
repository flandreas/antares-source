package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.net.*
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * An interactive switch with two bi-directional [DigitalPort]s
 */
class RealSwitch(
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractSwitch<RealSwitch>(CALCULATOR), NetCombiner, NetTopologyChanger {

	companion object {

		private val LOG by logger(RealSwitch::class)
		private const val BASE_RESOURCE_KEY = "library.element.RealSwitch"

		private val CALCULATOR = Calculator()

		private class Calculator : AbstractSwitch.Companion.AbstractSwitchCalculator<RealSwitch>() {
			override fun calculate(vertice: RealSwitch, data: GraphActorData, signalHandler: SignalHandler) {
				LOG.trace("calculate")
				super.calculate(vertice, data, signalHandler)
				if (data.changedPort != null) {
					handleInputChanged(data, vertice, signalHandler)
				} else {
					handleStateChanged(vertice, signalHandler)
				}
			}

			private fun handleInputChanged(data: GraphActorData, vertice: RealSwitch, signalHandler: SignalHandler) {
				if (vertice.isOn) {
					LOG.trace("handleInputChanged")
					val outputPortId = if (data.changedPort!!.portId == 1) 2 else 1
					vertice.getOutput<DigitalSignal>(outputPortId).setOutgoingSignalBuffered(data.getSignal(data.changedPort!!.portId), signalHandler)
				}
			}

			private fun handleStateChanged(vertice: RealSwitch, signalHandler: SignalHandler) {
				LOG.trace("handleStateChanged")
				val port1 = vertice.getPort<DigitalSignal>(1) as DigitalPort
				val port2 = vertice.getPort<DigitalSignal>(2) as DigitalPort
				port1.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)
				port2.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)

				if (vertice.isOn) {
					// Make sure that re-propagation from origin OutputPort is not blocked at InputPort
					// of this RealSwitch because incoming signal is already set
					(port1 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
					(port2 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
				}
			}
		}
	}

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")
	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	var bitWidth: BitWidth
		get() = (getPort<DigitalSignal>(1) as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				(getPort<DigitalSignal>(1) as DigitalPort).bitWidth = value
				(getPort<DigitalSignal>(2) as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	init {
		propagationDelay = 0
		addPort(DigitalPortImpl.createInOut(Logic.POSITIVE, null, bitWidth))
		addPort(DigitalPortImpl.createInOut(Logic.POSITIVE, null, bitWidth))
	}

	/** ---- [NetTopologyChanger] interface */

	private val netTopologyChangeListeners = mutableListOf<NetTopologyChangeListener>()

	override fun addNetTopologyChangeListener(listener: NetTopologyChangeListener) {
		if (!netTopologyChangeListeners.contains(listener)) {
			netTopologyChangeListeners.add(listener)
		}
	}

	override fun removeNetTopologyChangeListener(listener: NetTopologyChangeListener) {
		netTopologyChangeListeners.remove(listener)
	}

	private fun notifyNetTopologyChanged(signalHandler: SignalHandler) {
		LOG.trace("notifyNetTopologyChanged")
		val event = NetTopologyChangeEvent(this, signalHandler)
		netTopologyChangeListeners.toList().forEach { it.invoke(event) }
	}

	/** ---- [NetCombiner] interface */

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = true

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> {
		return if (isOn) {
			CombinedNet.createFor(outputPort, signalHandler)
		} else {
			emptyList()
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (bitWidth != BitWidth.BW_1) {
			writer.writeInt("bitWidth", bitWidth.width)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("bitWidth")) {
			bitWidth = BitWidth.of(reader.readInt("bitWidth"))
		}
	}

	/** ---- [AbstractSwitch] */

	override fun setState(signalHandler: SignalHandler, on: Boolean) {
		isOn = on
		notifyNetTopologyChanged(signalHandler)
		super.setState(signalHandler, on)
	}

	override fun createSignal(): DigitalSignal = DigitalSignalFactory.allOf(bitWidth, Bit.of(isOn))
}