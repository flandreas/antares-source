package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.graph.model.GraphElementListener
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.max
import kotlin.math.min

/**
 * A [Vertice] that collects signals from multiple [OscilloscopeProbeVertice]s.
 * [Oscilloscope] has a variable amount of [InputPort]s. Changes of values at the [InputPort]
 * are not processed through the [SignalHandler], but directly communicated to registered [GraphElementListener]s.
 */
class Oscilloscope(
	private val portFactory: PortFactory = GraphModelModule.portFactory
) : AbstractVertice() {

	companion object {
		private val LOG by logger(Oscilloscope::class)
		private val type = Translations.getString("graph.component.oscilloscope")
	}

	/** Maps a probe row number (starting with "1") to its [SignalHistory].*/
	private val signalHistories = mutableMapOf<String, SignalHistoryImpl<Any>>()

	/**
	 * Holds the maximum time of all [SignalHistoryEntry] in all [SignalHistories][SignalHistory]
	 * or 0 if not determined.
	 */
	var maxTime: Long = 0
		private set

	/**
	 * Holds the minimum non-zero time between two signals in any [SignalHistories][SignalHistory],
	 * or [Long.MAX_VALUE] if not determined.
	 */
	var minDiffTime: Long = Long.MAX_VALUE
		private set

	var overallMinDelay: Long = Long.MAX_VALUE
		private set

	/** ---- [AbstractVertice] */

	override val type: String get() = Oscilloscope.type
	override val typeDesc: String get() = ""

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
		storeSignal(input, signalHandler)
		stateChanged(signalHandler)
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("portsCount", portsCount)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		for (i in 1..reader.readInt("portsCount")) {
			val port = portFactory.createPort<Any>(PortType.INPUT)
			port.name = i.toString()
			addPort(port)
		}
	}

	/** ---- [Actor] interface */

	override fun executionStarted(signalHandler: SignalHandler) {
		signalHistories.clear()
		maxTime = 0
		minDiffTime = Long.MAX_VALUE
		overallMinDelay = Long.MAX_VALUE

		getPorts().forEach { signalHistories[it.name!!] = SignalHistoryImpl() }
		super.executionStarted(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		signalHistories.clear()
		super.executionStopped(signalHandler)
	}

	/** ---- [Oscilloscope] */

	fun getSignalHistory(rowNumber: String): SignalHistory<Any>? {
		return signalHistories[rowNumber]
	}

	/**
	 * Removes all entries from all [SignalHistories][SignalHistory] of this [Oscilloscope]
	 * that are older than the specified time.
	 */
	fun truncate(time: Long) {
		signalHistories.forEach { it.value.truncate(time) }
	}

	private fun storeSignal(input: InputPort<*>, signalHandler: SignalHandler) {
		val signal = input.getIncomingSignal()!!
		val history = signalHistories[input.name!!]!!
		LOG.debug("Oscilloscope ${input.name}: storing signal '$signal' at time ${signalHandler.executionTime}")
		history.add(SignalHistoryEntry(signal, signalHandler.executionTime))
		updateMaxTime(signalHandler.executionTime)
		updateMinDiffTime(signalHandler.executionTime)
		updateOverallMinDelay(history.minDelay)
	}

	private fun updateMaxTime(now: Long) {
		maxTime = max(maxTime, now)
		LOG.debug("maxTime = $maxTime")
	}

	private fun updateMinDiffTime(now: Long) {
		val minSignalHistory = signalHistories.values
			.filter { it.size > 0 && it.last().time != now }
			.minBy { now - it.last().time }
		if (minSignalHistory != null) {
			minDiffTime = min(minDiffTime, now - minSignalHistory.last().time)
		}
		LOG.debug("minDiffTime = $minDiffTime")
	}

	private fun updateOverallMinDelay(minDelay: Long) {
		overallMinDelay = min(overallMinDelay, minDelay)
		LOG.debug("overallMinDelay = $overallMinDelay")
	}
}