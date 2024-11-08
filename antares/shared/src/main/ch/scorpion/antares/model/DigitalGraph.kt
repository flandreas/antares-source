package ch.scorpion.antares.model

import ch.scorpion.antares.model.net.*
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.testcase.Testcases
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphExecutionContext
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.model.nonvolatile.NonVolatileStorable
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Extends [GraphImpl] in order to create temporary [Net]s for [Tunnel]s during execution.
 */
class DigitalGraph(
	name: TranslatableText = TranslatableText(Translations.getString("graph.name.unknown")),
	eventBus: EventBus = BaseModule.eventBus
) : GraphImpl(name = name, type = AntaresGraphTypes.Digital, eventBus = eventBus) {

	companion object {
		private val DEF_NET_SIGNAL_APPLIER_STRATEGY = NetSignalApplierStrategy.Conflict

		private val GLOBAL_NETS = mutableMapOf<SignalHandler, MutableMap<String, Net<DigitalSignal>>>()
	}

	var netSignalApplierStrategy: NetSignalApplierStrategy = DEF_NET_SIGNAL_APPLIER_STRATEGY

	var testcases: Testcases = Testcases(this, eventBus)
		private set

	/** ---- [GraphImpl] */

	override fun formNet(signalHandler: SignalHandler) {
		createTunnelNets(signalHandler)
		super.formNet(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler, nonVolatileData: NonVolatileStorable?) {
		super.executionStopped(signalHandler, nonVolatileData)
		destroyLocalTunnelNets()
		destroyGlobalTunnelNets(signalHandler)
	}

	override fun <T : Any> createGraphExecutionContext(): GraphExecutionContext<T> =
		GraphExecutionContext(netSignalApplierStrategy.netSignalApplier) as GraphExecutionContext<T>

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (netSignalApplierStrategy != DEF_NET_SIGNAL_APPLIER_STRATEGY) {
			writer.writeString("netSignalApplier", netSignalApplierStrategy.customName)
		}
		if (!testcases.isEmpty) {
			writer.writeStorable("testcases", testcases)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("netSignalApplier")) {
			netSignalApplierStrategy = NetSignalApplierStrategy.withName(reader.readString("netSignalApplier"))
		}
		if (reader.hasElement("testcases")) {
			testcases = reader.readStorable("testcases") as Testcases
			testcases.graph = this
		}
	}

	/** ---- [DigitalGraph] */

	val tunnelNames: Set<TunnelName> get() = elements
		.filterIsInstance<Tunnel>()
		.mapNotNull { it.tunnelName }
		.toSet()

	private fun createTunnelNets(signalHandler: SignalHandler) {
		// If already connected, this DigitalGraph is already open via a SubGraphVerticeRef,
		// and its Tunnel nets have already been indirectly created
		val localTunnelNets = mutableMapOf<String, Net<DigitalSignal>>()
		elements
			.filterIsInstance<Tunnel>()
			.filter { StringUtils.isNotEmpty(it.name) }
			.forEach { tunnel ->
				if (tunnel.isGlobal) {
					val netMap = GLOBAL_NETS.getOrPut(signalHandler) { mutableMapOf() }
					netMap
						.getOrPut(tunnel.name!!) { DigitalNet() }
						.also {
							if (!tunnel.invisiblePort.isConnected) {
								it.connect(tunnel.invisiblePort)
							}
						}
				} else {
					localTunnelNets
						.getOrPut(tunnel.name!!) { DigitalNet().apply { add(this) } }
						.also {
							if (!tunnel.invisiblePort.isConnected) {
								it.connect(tunnel.invisiblePort)
							}
						}
				}
		}
	}

	private fun destroyLocalTunnelNets() {
		val nets = mutableSetOf<Net<*>>()
		elements.filterIsInstance<Tunnel>().forEach { tunnel ->
			val port = tunnel.invisiblePort
			port.net?.let {
				nets.add(it)
				it.unconnect(port)
			}
		}
		nets.forEach { remove(it) }
	}

	private fun destroyGlobalTunnelNets(signalHandler: SignalHandler) {
		elements
			.filterIsInstance<Tunnel>()
			.filter { it.isGlobal }
			.forEach { tunnel ->
				tunnel.invisiblePort.net?.unconnect(tunnel.invisiblePort)
			}
		GLOBAL_NETS.remove(signalHandler)
	}
}