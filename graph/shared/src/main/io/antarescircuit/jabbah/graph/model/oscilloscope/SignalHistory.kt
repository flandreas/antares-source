package io.antarescircuit.jabbah.graph.model.oscilloscope

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.graph.model.Graph

/** A signal value at a simulation time in nanoseconds. */
data class SignalHistoryEntry<out T : Any>(val signal: T, val time: Long)

/**
 * Bounded signal changes recorded while executing a [Graph], in ascending time order.
 * A deque avoids shifting the entire recording when its oldest entry expires.
 */
class SignalHistory<T : Any>(private val bufferSize: Int) {

	companion object {
		private val LOG by logger(SignalHistory::class)
	}

	init {
		require(bufferSize >= 0) { "Buffer size must not be negative" }
	}

	private val entries = ArrayDeque<SignalHistoryEntry<T>>()

	/** Monotonic queues keep the retained minimum and maximum current in amortized O(1). */
	private val minima = ArrayDeque<SignalHistoryEntry<T>>()
	private val maxima = ArrayDeque<SignalHistoryEntry<T>>()

	val size: Int get() = entries.size

	val isEmpty: Boolean get() = entries.isEmpty()

	var overflow: Boolean = bufferSize == 0
		private set

	/** The smallest retained comparable signal, or null for non-comparable signals. */
	val minimum: T? get() = minima.firstOrNull()?.signal

	/** The largest retained comparable signal, or null for non-comparable signals. */
	val maximum: T? get() = maxima.firstOrNull()?.signal

	val maxTime: Long get() = entries.lastOrNull()?.time ?: 0

	/** Returns the latest change at or before [time], including the last change at an equal timestamp. */
	fun getEntryAt(time: Long): SignalHistoryEntry<T>? {
		val index = upperBound(time) - 1
		return if (index >= 0) entries[index] else null
	}

	/** Iterates retained changes at or after [startTime], without copying the recording. */
	fun getEntriesSince(startTime: Long): Iterator<SignalHistoryEntry<T>> = entries.listIterator(lowerBound(startTime))

	/** Iterates retained changes at or after [startTime], newest first, without copying the recording. */
	fun getReverseEntriesUntil(startTime: Long): Iterator<SignalHistoryEntry<T>> {
		val first = lowerBound(startTime)
		val iterator = entries.listIterator(entries.size)
		return object : Iterator<SignalHistoryEntry<T>> {
			override fun hasNext(): Boolean = iterator.previousIndex() >= first
			override fun next(): SignalHistoryEntry<T> {
				if (!hasNext()) throw NoSuchElementException()
				return iterator.previous()
			}
		}
	}

	fun last(): SignalHistoryEntry<T> = entries.last()

	fun lastOrNull(): SignalHistoryEntry<T>? = entries.lastOrNull()

	fun dispose() {
		clear()
	}

	fun clear() {
		entries.clear()
		minima.clear()
		maxima.clear()
		overflow = bufferSize == 0
	}

	fun add(signal: T, time: Long) {
		add(SignalHistoryEntry(signal, time))
	}

	fun add(entry: SignalHistoryEntry<T>) {
		require(entries.isEmpty() || entries.last().time <= entry.time)
		if (bufferSize == 0 || entries.lastOrNull()?.signal == entry.signal) return

		if (size == bufferSize) {
			val expired = entries.removeFirst()
			if (minima.firstOrNull() === expired) minima.removeFirst()
			if (maxima.firstOrNull() === expired) maxima.removeFirst()
			overflow = true
		}
		entries.addLast(entry)
		if (entry.signal is Comparable<*>) {
			updateMinMax(entry)
		}
	}

	fun logContent() {
		for (entry in entries) {
			LOG.trace("- ${entry.time}: ${entry.signal}")
		}
	}

	/** First entry whose timestamp is at least [time]. */
	private fun lowerBound(time: Long): Int {
		var low = 0
		var high = entries.size
		while (low < high) {
			val middle = low + (high - low) / 2
			if (entries[middle].time < time) low = middle + 1 else high = middle
		}
		return low
	}

	/** First entry whose timestamp is greater than [time]. */
	private fun upperBound(time: Long): Int {
		var low = 0
		var high = entries.size
		while (low < high) {
			val middle = low + (high - low) / 2
			if (entries[middle].time <= time) low = middle + 1 else high = middle
		}
		return low
	}

	@Suppress("UNCHECKED_CAST") // Called only for comparable signals, as in the original history implementation.
	private fun updateMinMax(entry: SignalHistoryEntry<T>) {
		val signal = entry.signal as Comparable<Any>
		while (minima.isNotEmpty() && signal < minima.last().signal) minima.removeLast()
		while (maxima.isNotEmpty() && signal > maxima.last().signal) maxima.removeLast()
		minima.addLast(entry)
		maxima.addLast(entry)
	}
}
