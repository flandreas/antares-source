package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.execution.SignalHandler

/**
 * Manages the interaction of an [Actor] with its [ActorListener]s and the [SignalHandler].
 */
class ActorSupport(private val actor: Actor) {

    /**
     * Maps a registered [ActorListener] to its [Entry] object.
     * Manual lazy initialization because [hasListeners] property would unnecessarily instantiate a Kotlin lazy delegate.
     */
    private var entries: MutableMap<ActorListener, Entry>? = null

    val hasListeners: Boolean
        get() = entries != null && !entries!!.isEmpty()

    fun addListener(l: ActorListener) {
        ensureEntry(l)
    }

    fun removeListener(l: ActorListener) {
        entries?.remove(l)
    }

    fun requestActingAfter(signalHandler: SignalHandler, delay: Long, data: ActorData) {
        signalHandler.requestActingAfter(actor, delay, data)
        notifyActingRequested(signalHandler, data)
    }

    fun requestActingTimeFreeze(signalHandler: SignalHandler, data: ActorData) {
        signalHandler.requestActingTimeFreeze(actor, data)
        notifyActingRequested(signalHandler, data)
    }

    fun notifyActed(signalHandler: SignalHandler, data: ActorData) {
        if (entries != null && entries!!.isNotEmpty()) {
            entries?.forEach {
                it.value.isPending = true
                it.key.acted(actor, signalHandler, data)
            }
        } else {
	        signalHandler.actingDone(actor, data)
        }
    }

    fun actingVisualized(signalHandler: SignalHandler, l: ActorListener, data: ActorData?) {
        if (entries != null) {
            ensureEntry(l).isPending = false
        }
        if (allDone()) {
            signalHandler.actingDone(actor, data)
        }
    }

    private fun ensureEntry(l: ActorListener): Entry {
        if (entries == null) {
            entries = mutableMapOf()
        }
        return entries!!.getOrPut(l) {Entry(l)}
    }

    private fun allDone(): Boolean {
        if (entries == null) {
            return true
        }
        return entries!!.values.all { !it.isPending }
    }

    private fun notifyActingRequested(signalHandler: SignalHandler, data: ActorData) {
        entries?.forEach {
            it.value.isPending = false
            it.key.actingRequested(actor, signalHandler, data)
        }
    }

    /** Holds a registered [ActorListener] along with the information whether it is currently pending.*/
    private data class Entry(val listener: ActorListener, var isPending: Boolean = false)
}