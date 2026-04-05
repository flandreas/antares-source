package io.antarescircuit.antares.model.input

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.BitWidthExpression
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.vertice.AdjustableBitWidth
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.net.NetCombiner
import io.antarescircuit.jabbah.graph.model.net.NetTopologyChangeEvent
import io.antarescircuit.jabbah.graph.model.net.NetTopologyChangeListener
import io.antarescircuit.jabbah.graph.model.net.NetTopologyChanger
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

abstract class AbstractRealSwitch<T : AbstractSwitch<T>>(
	calculator: VerticeCalculator<T>,
	portCount: Int,
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractSwitch<T>(calculator), NetCombiner, NetTopologyChanger, AdjustableBitWidth {

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
		propagationDelay = LongValueImpl.ZERO
		for (i in 1..portCount) {
			addPort(DigitalPortImpl.createInOut(Logic.POSITIVE, null, bitWidth))
		}
	}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(portInt: Int, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		return true
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
		if (bitWidth is BitWidthExpression || bitWidth.width != BitWidth.BW_1.width) {
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

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		// Make sure that switch is off at start of next simulation run
		requestSetSignal(false, signalHandler)
	}

	/** ---- [AbstractSwitch] */

	override fun requestSetSignal(signal: Boolean, signalHandler: SignalHandler) {
		this.signal = signal
		notifyNetTopologyChanged(signalHandler)
		super.requestSetSignal(signal, signalHandler)
	}
}