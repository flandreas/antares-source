package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthExpression
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.net.NetTopologyChangeEvent
import ch.scorpion.jabbah.graph.model.net.NetTopologyChangeListener
import ch.scorpion.jabbah.graph.model.net.NetTopologyChanger
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

abstract class AbstractRealSwitch<T : AbstractSwitch<T>>(
	calculator: VerticeCalculator<T>,
	portCount: Int,
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractSwitch<T>(calculator), NetCombiner, NetTopologyChanger {

	companion object {
		private val LOG by logger(AbstractRealSwitch::class)

		abstract class AbstractCalculator<T : AbstractSwitch<T>> : AbstractSwitch.Companion.AbstractSwitchCalculator<T>() {

			override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
				super.calculate(vertice, data, signalHandler)
				if (data.changedPort != null) {
					handleInputChanged(data, vertice, signalHandler)
				} else {
					handleStateChanged(data, vertice, signalHandler)
				}
			}

			protected abstract fun handleInputChanged(data: GraphActorData, vertice: T, signalHandler: SignalHandler)
			protected abstract fun handleStateChanged(data: GraphActorData, vertice: T, signalHandler: SignalHandler)
		}
	}

	var bitWidth: BitWidth
		get() = (getPort<DigitalSignal>(1) as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				getPorts().map { it as DigitalPort }.forEach { it.bitWidth = value }
				stateChanged()
			}
		}

	init {
		propagationDelay = 0
		for (i in 1..portCount) {
			addPort(DigitalPortImpl.createInOut(Logic.POSITIVE, null, bitWidth))
		}
	}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
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

	override fun containsNetTopologyChangeListener(listener: NetTopologyChangeListener): Boolean =
		netTopologyChangeListeners.contains(listener)

	private fun notifyNetTopologyChanged(signalHandler: SignalHandler) {
		LOG.trace("notifyNetTopologyChanged")
		val event = NetTopologyChangeEvent(this, signalHandler)
		netTopologyChangeListeners.toList().forEach { it.handle(event) }
	}

	protected fun askNetTopologyChangeListenersForResend(signalHandler: SignalHandler) {
		netTopologyChangeListeners.forEach { it.resendSignal(signalHandler) }
	}

	/** ---- [NetCombiner] interface */

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = false

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (bitWidth.width != BitWidth.BW_1.width) {
			bitWidth.write("bitWidth", writer)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("bitWidth")) {
			bitWidth = BitWidth.read("bitWidth", reader)
		}
	}

	/** ---- [Actor] interface */

	override fun executionStart(signalHandler: SignalHandler) {
		// Real switches are passive and don't publish signals at startup
	}

	/** ---- [AbstractSwitch] */

	override fun setState(signalHandler: SignalHandler, on: Boolean) {
		isOn = on
		notifyNetTopologyChanged(signalHandler)
		super.setState(signalHandler, on)
	}
}