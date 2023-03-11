package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

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
		private val TYPE get() = Translations.getString("graph.component.oscilloscope.name")
	}

	private lateinit var signalHistories: SignalHistories

	var mode: SignalHistoriesType = mode

	var enabled: Boolean = true

	var graphType: GraphType = graphType
		private set

	/** ---- [AbstractVertice] */

	override val type: String get() = TYPE
	override val typeDesc: String get() = ""

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler, force: Boolean) {
		if (enabled) {
			signalHistories.storeSignal(input, signalHandler)
			stateChanged(signalHandler)
		}
	}

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
		signalHistories = mode.createSignalHistories(this)
		super.executionInitialize(signalHandler)
	}

	/** ---- [Oscilloscope] */

	val maxTime: Long get() = signalHistories.maxTime

	fun getSignalHistory(name: String): SignalHistory<Any>? = signalHistories.getSignalHistory(name)
}