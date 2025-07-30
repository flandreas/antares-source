package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.execution.SignalHandler

/**
 * A [Net] is a [GraphElement] that can forward signals between [Port]s of [Vertice]s
 * @param T the type of signals that this [Net] forwards
 */
interface Net<T: Any> : GraphElement {

	companion object {

		/** Used in [GraphElementEvent] sent to [GraphElementListener]s, typically views. */
		const val STATE_CHANGE_SIGNAL = "signal"

		val BROKEN_REF_DESIGN_ERROR = DesignError(Translations.getString("graph.designError.brokenPortRef.text"))
	}

    /** The signal of this [Net] after an execution step has been done.*/
    val signal: T?

	/** Returns a displayable description of the [Net]'s current signal.*/
	val signalDescription: String? get() = signal?.toString() ?: ""

    /** Buffers the signal during an execution step.*/
    val signalBuffer: T?

    /** Returns the number of connected [Port]s of this [Net].*/
    val portsCount: Int

    /** Returns the [Port]s to which this [Net] is connected as an immutable list.*/
    val ports: ImmutableList<Port<T>>

    /** Returns all [Port]s of [ports] with [WeakOutputPortBehaviour]*/
    val weakOutputPorts: Collection<OutputPort<T>>

	val hasConflictingOutputs: Boolean

    /** Connects the specified [Port] with this [Net]. */
    fun connect(port: Port<T>)

    /** Unconnects the specified [Port] from this [Net].*/
    fun unconnect(port: Port<*>)

    /** Checks whether the specified [Port] is connected with this [Net].*/
    fun isConnectedWith(port: Port<out T>): Boolean

    /**
     * Sets the current signal of this [Net] and forwards it to all connected [Port]s.
     * @param signal the signal to set.
     * @param origin the [OutputPort] that sends `signal` into this [Net].
     * @param signalHandler the runtime interface to the execution subsystem.
     */
    fun setSignal(signal: T?, origin: OutputPort<T>, immediatePort: OutputPort<T>, signalHandler: SignalHandler, force: Boolean)

    /** Creates a new [Net] of the same type as this [Net] without copying all the [Port]s.*/
    fun cloneEmpty(): Net<T>

	/**
	 * Creates a new [Net] of the same type as this [Net], and reconnects those [Port]s of
	 * this [Net] that are also in [ports] to the newly created [Net].
	 */
	fun splitOff(ports: Set<Port<T>>): Net<T>

	/**
	 * Unconnects all [Ports][Port] in [other] and connects them with this [Net].
	 * Does NOT remove [other] from its owning [Graph]. This is the responsibility of calling objects.
	 */
	fun combine(other: Net<T>)

	/**
	 * Activate the design error [BROKEN_REF_DESIGN_ERROR] on this [Net].
	 * Used for hunting the cause of bug #584.
	 */
	fun activateBrokenRefError()
}

