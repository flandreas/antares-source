package ch.scorpion.jabbah.graph.model.oscilloscope

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

	val overflow: Boolean

	/** Returns the number of entries in this [SignalHistory].*/
	val size: Int

	/** Returns the minimum signal value, or `null` if [T] isn't comparable.*/
	val minimum: T?

	/** Returns the maximum signal value, or `null` if [T] isn't comparable.*/
	val maximum: T?

	val maxTime:Long

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
class SignalHistoryImpl<T : Any>(
	private val bufferSize: Int
) : SignalHistory<T> {

	/** Holds the entries of this [SignalHistoryImpl], having the newest entry as the last position.*/
	private val entries = mutableListOf<SignalHistoryEntry<T>>()

	private var minimumEntry: SignalHistoryEntry<T>? = null
	private var maximumEntry: SignalHistoryEntry<T>? = null

	/** ---- [SignalHistory] interface */

	/** Returns the number of entries in this [SignalHistory].*/
	override val size: Int get() = entries.size

	override var overflow: Boolean = bufferSize == 0
		private set

	override val minimum: T? get() = minimumEntry?.signal

	override val maximum: T? get() = maximumEntry?.signal

	override val maxTime: Long get() = entries.maxOfOrNull { it.time } ?: 0

	/** Iterates the [SignalHistoryEntries][SignalHistoryEntry] since the specified execution time in ascending time order.*/
	override fun getEntriesSince(startTime: Long): Iterator<SignalHistoryEntry<T>> =
		entries.filter { it.time >= startTime }.iterator()

	/** Iterates the [SignalHistoryEntries][SignalHistoryEntry] in reverse order, starting with the newest entry.*/
	override fun getReverseEntriesUntil(startTime: Long): Iterator<SignalHistoryEntry<T>> =
		entries.asReversed().filter { it.time >= startTime }.iterator()

	override fun last(): SignalHistoryEntry<T> = entries.last()

	/** Returns the last [SignalHistoryEntry], i.e. the one with the most recent time.*/
	override fun lastOrNull(): SignalHistoryEntry<T>? = entries.lastOrNull()

	/** ---- [SignalHistoryImpl] */

	fun dispose() {
		clear()
	}

	fun clear() {
		entries.clear()
		overflow = bufferSize == 0
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