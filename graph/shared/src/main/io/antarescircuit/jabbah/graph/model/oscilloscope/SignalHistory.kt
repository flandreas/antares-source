package io.antarescircuit.jabbah.graph.model.oscilloscope

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.graph.model.Graph

/**
 * A single entry in a [SignalHistory] that represents a signal value at a particular time.
 *
 * @param T the type of the signal
 * @property signal the value of the signal
 * @property time the simulation time (in ns) when the signal occurred
 */
data class SignalHistoryEntry<out T : Any>(val signal: T, val time: Long)

/**
 * Stores changes of signal values while execution of a [Graph].
 * [SignalHistoryEntries][SignalHistoryEntry] must be added by clients of this class
 * in ascending time order.
 */
class SignalHistory<T : Any>(private val bufferSize: Int) {

	companion object {
		private val LOG by logger(SignalHistory::class)
	}

	/** Holds the entries of this [SignalHistory], having the newest entry as the last position.*/
	private val entries = mutableListOf<SignalHistoryEntry<T>>()

	private var minimumEntry: SignalHistoryEntry<T>? = null
	private var maximumEntry: SignalHistoryEntry<T>? = null

	/** Returns the number of entries in this [SignalHistory].*/
	val size: Int get() = entries.size

	val isEmpty: Boolean get() = size == 0

	var overflow: Boolean = bufferSize == 0
		private set

	/** Returns the minimum signal value, or `null` if [T] isn't comparable.*/
	val minimum: T? get() = minimumEntry?.signal

	/** Returns the maximum signal value, or `null` if [T] isn't comparable.*/
	val maximum: T? get() = maximumEntry?.signal

	val maxTime: Long get() = entries.maxOfOrNull { it.time } ?: 0

	/**
	 * Returns the [SignalHistoryEntry] with the largest [SignalHistoryEntry.time] smaller than or equal to [time].
	 * This corresponds with the way a curve is drawn using horizontal segments.
	 */
	fun getEntryAt(time: Long): SignalHistoryEntry<T>? =
		entries.filter { it.time <= time }.maxByOrNull { it.time }

	/** Iterates the [SignalHistoryEntries][SignalHistoryEntry] since the specified execution time in ascending time order.*/
	fun getEntriesSince(startTime: Long): Iterator<SignalHistoryEntry<T>> =
		entries.filter { it.time >= startTime }.iterator()

	/** Iterates the [SignalHistoryEntries][SignalHistoryEntry] in reverse order, starting with the newest entry.*/
	fun getReverseEntriesUntil(startTime: Long): Iterator<SignalHistoryEntry<T>> =
		entries.asReversed().filter { it.time >= startTime }.iterator()

	fun last(): SignalHistoryEntry<T> = entries.last()

	/** Returns the last [SignalHistoryEntry], i.e. the one with the most recent time.*/
	fun lastOrNull(): SignalHistoryEntry<T>? = entries.lastOrNull()

	fun dispose() {
		clear()
	}

	fun clear() {
		entries.clear()
		overflow = bufferSize == 0
		minimumEntry = null
		maximumEntry = null
	}

	fun add(signal: T, time: Long) {
		add(SignalHistoryEntry(signal, time))
	}

	fun add(entry: SignalHistoryEntry<T>) {
		require(entries.isEmpty() || entries.last().time <= entry.time)

		var requireAllUpdateMinMax = false
		if (entries.isEmpty() || entries.last().signal != entry.signal) {
			if (size == bufferSize) {
				requireAllUpdateMinMax = entries[0] === minimumEntry || entries[0] === maximumEntry
				entries.removeAt(0)
				overflow = true
			}
			entries.add(entry)

			if (entry.signal is Comparable<*>) {
				if (requireAllUpdateMinMax) {
					updateAllMinMax()
				} else {
					updateMinMax(entry)
				}
			}
		}
	}

	fun logContent() {
		for (e in entries) {
			LOG.trace("- ${e.time}: ${e.signal}")
		}
	}

	private fun updateAllMinMax() {
		minimumEntry = null
		maximumEntry = null
		entries.forEach { updateMinMax(it) }
	}

	private fun updateMinMax(entry: SignalHistoryEntry<T>) {
		if (minimumEntry == null || (minimumEntry!!.signal as Comparable<Any>) > entry.signal as Comparable<Any>) {
			minimumEntry = entry
		}
		if (maximumEntry == null || (maximumEntry!!.signal as Comparable<Any>) < entry.signal as Comparable<Any>) {
			maximumEntry = entry
		}
	}
}