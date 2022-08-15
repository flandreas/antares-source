package ch.scorpion.antares.model

import ch.scorpion.antares.model.net.*
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphExecutionContext
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Extends [GraphImpl] in order to create temporary [Net]s for [Tunnel]s during execution.
 */
class DigitalGraph(
	name: String = Translations.getString("graph.name.unknown"),
	eventBus: EventBus = BaseModule.eventBus
) : GraphImpl(name = name, eventBus = eventBus) {

	companion object {
		private val DEF_NET_SIGNAL_APPLIER_STRATEGY = NetSignalApplierStrategy.Conflict
	}

	var netSignalApplierStrategy: NetSignalApplierStrategy = DEF_NET_SIGNAL_APPLIER_STRATEGY

	/** ---- [GraphImpl] */

	override fun formNet(signalHandler: SignalHandler) {
		createTunnelNets()
		super.formNet(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		destroyTunnelNets()
	}

	override fun <T : Any> createGraphExecutionContext(): GraphExecutionContext<T> =
		GraphExecutionContext(netSignalApplierStrategy.netSignalApplier) as GraphExecutionContext<T>

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (netSignalApplierStrategy != DEF_NET_SIGNAL_APPLIER_STRATEGY) {
			writer.writeString("netSignalApplier", netSignalApplierStrategy.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("netSignalApplier")) {
			netSignalApplierStrategy = NetSignalApplierStrategy.withName(reader.readString("netSignalApplier"))
		}
	}

	/** ---- [DigitalGraph] */

	private fun createTunnelNets() {
		val tunnelNets = mutableMapOf<String, Net<DigitalSignal>>()
		elements
			.filterIsInstance<Tunnel>()
			.filter { StringUtils.isNotEmpty(it.name) }
			.forEach { tunnel ->
				tunnelNets
					.getOrPut(tunnel.name!!) { DigitalNet().apply { add(this) } }
					.also {
						it.connect(tunnel.getPort(2))
					}
		}
	}

	private fun destroyTunnelNets() {
		val nets = mutableSetOf<Net<*>>()
		elements.filterIsInstance<Tunnel>().forEach { tunnel ->
			val port = tunnel.getPort<DigitalSignal>(2)
			port.net?.let {
				nets.add(it)
				it.unconnect(port)
			}
		}
		nets.forEach { remove(it) }
	}
}