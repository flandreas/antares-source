package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.graph.model.Graph

/**
 * A single entry in a [SignalHistory] that represents a signal value at a particular time.
 *
 * @param T the type of the signal
 * @property signal the value of the signal
 * @property time the simulation time (in ns) when the signal occurred
 */
data class SignalHistoryEntry<out T : Any>(val signal: T, val time: Long)

interface SignalHistory<out T : Any> {

	val isEmpty: Boolean get() = size == 0

	/** Returns the number of entries in this [SignalHistory].*/
	val size: Int

	/** Returns the minimum time delay between any two subsequent entries, [Long.MAX_VALUE] if no entries available.*/
	val minDelay: Long

	fun last(): SignalHistoryEntry<T>

	fun lastOrNull(): SignalHistoryEntry<T>?

	/** Iterates the [SignalHistoryEntries][SignalHistoryEntry] since the specified execution time in ascending time order.*/
	fun getEntriesSince(startTime: Long): Iterator<SignalHistoryEntry<T>>

	fun getReverseEntriesUntil(startTime: Long): Iterator<SignalHistoryEntry<T>>
}

/**
 * Stores changes of signal values while execution of a [Graph].
 * [SignalHistoryEntries][SignalHistoryEntry] must be added by clients of this class
 * in ascending time order.
 */
class SignalHistoryImpl<T : Any> : SignalHistory<T> {

	/** Holds the entries of this [SignalHistoryImpl], having the newest entry as the last position.*/
	private val list = mutableListOf<SignalHistoryEntry<T>>()

	/** Backing field of [minDelay] that is always kept up-to-date.*/
	private var _minDelay: Long = Long.MAX_VALUE

	/** ---- [SignalHistoryImpl] */

	override val minDelay: Long get() = _minDelay

	/** Returns the number of entries in this [SignalHistory].*/
	override val size: Int get() = list.size

	/** Iterates the [SignalHistoryEntries][SignalHistoryEntry] since the specified execution time in ascending time order.*/
	override fun getEntriesSince(startTime: Long): Iterator<SignalHistoryEntry<T>> {
		return list.filter { it.time >= startTime }.iterator()
	}

	/** Iterates the [SignalHistoryEntries][SignalHistoryEntry] in reverse order, starting with the newest entry.*/
	override fun getReverseEntriesUntil(startTime: Long): Iterator<SignalHistoryEntry<T>> {
		return list.asReversed().filter { it.time >= startTime }.iterator()
	}

	override fun last(): SignalHistoryEntry<T> {
		return list.last()
	}

	/** Returns the last [SignalHistoryEntry], i.e. the one with the most recent time.*/
	override fun lastOrNull(): SignalHistoryEntry<T>? {
		return list.lastOrNull()
	}

	/** ---- [SignalHistoryImpl] */

	fun dispose() {
		clear()
	}

	fun clear() {
		list.clear()
	}

	fun add(signal: T, time: Long) {
		add(SignalHistoryEntry(signal, time))
	}

	fun add(entry: SignalHistoryEntry<T>) {
		checkArgument(list.isEmpty() || list.last().time <= entry.time)
		if (list.isEmpty() || list.last().signal != entry.signal) {
			if (!list.isEmpty()) {
				_minDelay = Math.min(minDelay, entry.time - list.last().time)
			}
			list.add(entry)
		}
	}

	/**
	 * Removes all [SignalHistoryEntries][SignalHistoryEntry] from this [SignalHistory] that are older
	 * than the specified time. This is only used to avoid memory exhaustion. This method does NOT recalculate
	 * [minDelay] by intention.
	 */
	fun truncate(time: Long) {
		list.removeAll { it.time < time }
	}

	private fun recalculateMinDelay() {
		_minDelay = if (isEmpty) Long.MAX_VALUE else list
			.mapIndexed { i: Int, e: SignalHistoryEntry<T> -> if (i == 0) Long.MAX_VALUE else e.time - list[i - 1].time }
			.min()!!
	}
}