package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphElementListener
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.max

/**
 * A [Vertice] that collects signals from multiple [OscilloscopeProbeVertice]s.
 * [Oscilloscope] has a variable amount of [InputPort]s. Changes of values at the [InputPort]
 * are not processed through the [SignalHandler], but directly communicated to registered [GraphElementListener]s.
 *
 * The maximum number of entries in every [SignalHistory] is confined by the [Properties] entry defined by
 * [PROP_BUFFER_SIZE].
 */
class Oscilloscope(
	private val portFactory: PortFactory = GraphModelModule.portFactory
) : AbstractVertice() {

	companion object {
		private val LOG by logger(Oscilloscope::class)
		private val TYPE get() = Translations.getString("graph.component.oscilloscope.name")

		/** The name of the [Int] property in [Properties] */
		const val PROP_BUFFER_SIZE = "Oscilloscope.bufferSize"
	}

	/** Maps a probe name to its [SignalHistory] containing then of buffered signals.*/
	private val signalHistories = mutableMapOf<String, SignalHistoryImpl<Any>>()

	/**
	 * Holds the maximum time of all [SignalHistoryEntry] in all [SignalHistories][SignalHistory]
	 * or 0 if not determined.
	 */
	var maxTime: Long = 0
		private set

	/** ---- [AbstractVertice] */

	override val type: String get() = TYPE
	override val typeDesc: String get() = ""

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler, force: Boolean) {
		storeSignal(input, signalHandler)
		stateChanged(signalHandler)
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("portNames", StringUtils.fromList(
			getPorts().map { it.name!! }
		))
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		StringUtils.toList(reader.readString("portNames")).forEach {
			addPort(portFactory.createOscilloscopeProbePort(it))
		}
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		signalHistories.clear()
		maxTime = 0

		getPorts().forEach { signalHistories[it.name!!] = SignalHistoryImpl(bufferSize) }
		super.executionInitialize(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		signalHistories.clear()
		super.executionStopped(signalHandler)
	}

	/** ---- [Oscilloscope] */

	private val bufferSize: Int get() = BaseModule.properties.getInt(PROP_BUFFER_SIZE)

	fun getSignalHistory(name: String): SignalHistory<Any>? = signalHistories[name]

	private fun storeSignal(input: InputPort<*>, signalHandler: SignalHandler) {
		val signal = input.getIncomingSignal()!!
		val history = signalHistories[input.name!!]!!
		LOG.trace("Oscilloscope ${input.name}: storing signal '$signal' at time ${signalHandler.executionTime}")
		history.add(SignalHistoryEntry(signal, signalHandler.executionTime))
		updateMaxTime(signalHandler.executionTime)
	}

	private fun updateMaxTime(now: Long) {
		maxTime = max(maxTime, now)
		LOG.trace("maxTime = $maxTime")
	}
}