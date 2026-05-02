package io.antarescircuit.antares.model

import io.antarescircuit.antares.model.net.*
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.testcase.Testcases
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.properties.PropertyValueException
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphExecutionContext
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.graph.GraphImpl
import io.antarescircuit.jabbah.graph.model.nonvolatile.NonVolatileStorable
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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
		set(value) {
			if (!isReading && value == NetSignalApplierStrategy.Conflict) {
				val conflicts = elements
					.filterIsInstance<Net<*>>()
					.filter { it.hasConflictingOutputs }
					.toSet()
				if (conflicts.isNotEmpty()) {
                	throw PropertyValueException(
						Translations.getString("element.property.netSignalApplierStrategy.error"),
						NetSignalApplierFailure(conflicts))
				}
			}
			field = value
		}

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

	@Suppress("UNCHECKED_CAST")
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