package io.antarescircuit.jabbah.graph.model.oscilloscope

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.InputPort
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistories.Companion.PROP_BUFFER_SIZE
import kotlin.math.max

/**
 * Represents the supported types of [SignalHistory] to be used in an [Oscilloscope].
 */
enum class SignalHistoriesType(val customName: String) {

	Realtime("realtime") {
		override fun createSignalHistories(oscilloscope: Oscilloscope): SignalHistories = RealtimeSignalHistories(oscilloscope)
	},

	Clocked("clocked") {
		override fun createSignalHistories(oscilloscope: Oscilloscope): SignalHistories = ClockedSignalHistories(oscilloscope)
	};

	companion object {
		fun withName(customName: String): SignalHistoriesType =
			entries.firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown SignalHistoriesType $customName")
	}

	abstract fun createSignalHistories(oscilloscope: Oscilloscope): SignalHistories

	override fun toString(): String {
		return when (this) {
			Realtime -> Translations.getString("graph.property.oscilloscopeMode.type.realtime")
			Clocked -> Translations.getString("graph.property.oscilloscopeMode.type.clocked")
		}
	}
}

/**
 * Manages all [SignalHistory] of an [Oscilloscope] and handles the arrival of new incoming signals.
 */
interface SignalHistories {

	companion object {
		const val PROP_BUFFER_SIZE = "Oscilloscope.bufferSize"
	}

	val maxTime: Long

	fun clear()

	fun getSignalHistory(name: String): SignalHistory<Any>?

	fun storeSignal(name: String, signal: Any, signalHandler: SignalHandler)

	fun logContent()
}

abstract class AbstractSignalHistories(
	protected val oscilloscope: Oscilloscope
) : SignalHistories {

	companion object {
		private val LOG by logger(AbstractSignalHistories::class)
		private val bufferSize: Int get() = BaseModule.properties.getInt(PROP_BUFFER_SIZE)
	}

	/**
	 * Holds the maximum time of all [SignalHistoryEntry] in all [SignalHistories][SignalHistory]
	 * or 0 if not determined.
	 */
	override var maxTime: Long = 0
		protected set

	/** Maps a probe name to its [SignalHistory] containing then of buffered signals.*/
	protected val signalHistories = mutableMapOf<String, SignalHistory<Any>>()

	init {
		oscilloscope.getPorts().forEach { signalHistories[it.name!!] = SignalHistory(bufferSize) }
	}

	override fun clear() {
		signalHistories.values.forEach { it.clear() }
	}

	override fun getSignalHistory(name: String): SignalHistory<Any>? = signalHistories[name]

	protected fun updateMaxTime(now: Long) {
		maxTime = max(maxTime, now)
	}

	override fun logContent() {
		for (e in signalHistories.entries) {
			LOG.trace("Signal ${e.key}:")
			e.value.logContent()
		}
	}
}

/**
 * [RealtimeSignalHistories] adds signals to every [SignalHistory] using the real simulation time.
 */
class RealtimeSignalHistories(
	oscilloscope: Oscilloscope
) : AbstractSignalHistories(oscilloscope) {

	override fun storeSignal(name: String, signal: Any, signalHandler: SignalHandler) {
		val history = signalHistories[name]!!
		history.add(SignalHistoryEntry(signal, signalHandler.executionTime))
		updateMaxTime(signalHandler.executionTime)
	}
}

/**
 * [ClockedSignalHistories] adds signals to every [SignalHistory] based evenly spaced time slots
 * using an artificial time. This artificial time only advanced when a signal at the one-and-only
 * clocked [SignalHistory] changes, which corresponds with the top-most row in the [Oscilloscope].
 */
class ClockedSignalHistories(
	oscilloscope: Oscilloscope
) : AbstractSignalHistories(oscilloscope) {

	companion object {
		private const val DELTA_TIME = 100
	}

	/** The name of the [InputPort] that corresponds with the "clocked" [SignalHistory]. */
	private val clockPortName: String? = oscilloscope.getPorts().firstOrNull()?.name

	/** The current artificial time. */
	private var time: Long = 0

	override fun storeSignal(name: String, signal: Any, signalHandler: SignalHandler) {
		val history = signalHistories[name]!!
		if (name == clockPortName) {
			history.add(SignalHistoryEntry(signal, time))
			time += DELTA_TIME
			updateMaxTime(time)
		} else {
			history.add(signal, time)
		}
	}
}