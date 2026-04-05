package io.antarescircuit.jabbah.graph.model.oscilloscope

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.port.PortFactory
import io.antarescircuit.jabbah.graph.model.vertice.AbstractVertice
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A [Vertice] that collects signals from multiple [OscilloscopeProbeVertice]s.
 * [Oscilloscope] has a variable amount of [InputPort]s. Changes of values at the [InputPort]
 * are not processed through the [SignalHandler], but directly communicated to registered [GraphElementListener]s.
 *
 * The maximum number of entries in every [SignalHistory] is confined by the [Properties] entry defined by
 * [SignalHistories.PROP_BUFFER_SIZE].
 */
class Oscilloscope(
	mode: SignalHistoriesType = SignalHistoriesType.Clocked,
	graphType: GraphType = GenericGraphType,
	private val portFactory: PortFactory = GraphModelModule.portFactory
) : AbstractVertice() {

	companion object {
		private val LOG by logger(Oscilloscope::class)

		private val TYPE get() = Translations.getString("graph.component.oscilloscope.name")

		/** The reason in [GraphElementEvent] sent to [GraphElementListener]s if a new signal has arrived. */
		const val SIGNAL_RECEIVED = "signalReceived"
	}

	private var signalHistories: SignalHistories = mode.createSignalHistories(this)

	var mode: SignalHistoriesType = mode
		set(value) {
			if (field != value) {
				field = value
				signalHistories = mode.createSignalHistories(this)
			}
		}

	var enabled: Boolean = true

	var graphType: GraphType = graphType
		private set

	/** ---- [AbstractVertice] */

	override val type: String get() = TYPE
	override val typeDesc: String get() = ""

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("type", graphType.customName)
		writer.writeString("portNames", StringUtils.fromList(
			getPorts().map { it.name!! }
		))
		writer.writeString("mode", mode.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		graphType = if (reader.hasAttribute("type")) {
			GraphModelModule.graphTypeRegistry.withCustomName(reader.readString("type"))
		} else {
			GraphModelModule.defaultGraphType
		}
		StringUtils.toList(reader.readString("portNames")).forEach {
			addPort(portFactory.createOscilloscopeProbePort(it, graphType))
		}
		if (reader.hasAttribute("mode")) {
			mode = SignalHistoriesType.withName(reader.readString("mode"))
		}
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		signalHistories.clear()
		super.executionInitialize(signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		signalHistories = mode.createSignalHistories(this)
		super.executionStart(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		signalHistories.clear()
	}

	/** ---- [Oscilloscope] */

	val maxTime: Long get() = signalHistories.maxTime

	fun getSignalHistory(name: String): SignalHistory<Any>? = signalHistories.getSignalHistory(name)

	fun storeSignal(name: String, signal: Any, signalHandler: SignalHandler) {
		signalHistories.storeSignal(name, signal, signalHandler)
		if (LOG.isTraceEnabled()) {
			LOG.trace("--- Store signal $name with value $signal: SignalHistory content ---")
			signalHistories.logContent()
		}
	}

	fun handleSignal(probe: OscilloscopeProbeVertice<*>, signalHandler: SignalHandler) {
		if (enabled) {
			stateChanged(signalHandler, "$SIGNAL_RECEIVED:${probe.id}")
		}
	}
}