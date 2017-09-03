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
data class SignalHistoryEntry<out T: Any>(val signal: T, val time: Long)

/** Posted by [SignalHistory] on their [SignalHistoryListener]s after a new signal has been added.*/
data class SignalHistoryEvent(val signalHistory: SignalHistory<*>, val time: Long)

/** Called by [SignalHistory] after a new signal has been added.*/
interface SignalHistoryListener {
    fun handle(event: SignalHistoryEvent)
}

/**
 * Captures changes of signal values while execution of a [Graph].
 */
class SignalHistory<T: Any> {

    private val list = mutableListOf<SignalHistoryEntry<T>>()

    private val listeners = mutableListOf<SignalHistoryListener>()

    fun dispose() {
        clear()
        listeners.clear()
    }

    fun addListener(listener: SignalHistoryListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: SignalHistoryListener) {
        listeners.remove(listener)
    }

    /** Returns the number of entries in this [SignalHistory].*/
    fun size(): Int = list.size

    fun clear() {
        list.clear()
    }

    fun add(signal: T, time: Long) {
        add(SignalHistoryEntry(signal, time))
    }

    fun add(entry: SignalHistoryEntry<T>) {
        checkArgument(list.isEmpty() || list.last().time <= entry.time)
        if (list.isEmpty() || list.last().signal != entry.signal) {
            list.add(entry)
            val event = SignalHistoryEvent(this, entry.time)
            listeners.forEach { it.handle(event) }
        }
    }

    /** Iterates the [SignalHistoryEntries][SignalHistoryEntry] since the specified execution time.*/
    fun getEntries(startTime: Long): Iterator<SignalHistoryEntry<T>> {
        return list.filter { it.time >= startTime }.iterator()
    }
}