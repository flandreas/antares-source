package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.execution.SignalHandler

/**
 * A [Net] is a [GraphElement] that can forward signals between [Port]s of [Vertice]s
 * @param T the type of signals that this [Net] forwards
 */
interface Net<T: Any> : GraphElement {

    /** The signal of this [Net] after an execution step has been done.*/
    val signal: T?

	/** Returns a displayable description of the [Net]'s current signal.*/
	val signalDescription: String? get() = signal?.toString() ?: ""

    /** Buffers the signal during an execution step.*/
    val signalBuffer: T?

    /** Returns the number of connected [Port]s of this [Net].*/
    val portsCount: Int

    /**
     * Determines whether this [Net] has an inconsistent execution state. This can occur if a [Net] connects
     * multiple [OutputPort]s (which is allowed in order to support "tri-state behaviour"), and not all of these
     * [OutputPort]s assert the same signal to this [Net].
     */
    val inconsistent: Boolean

    /** Returns the [Port]s to which this [Net] is connected as an immutable list.*/
    val ports: ImmutableList<Port<T>>

    val weakOutputPorts: Collection<OutputPort<T>>

    /** Connects the specified [Port] with this [Net].*/
    fun connect(port: Port<T>)

    /** Unconnects the specified [Port] from this [Net].*/
    fun unconnect(port: Port<*>)

    /** Checks whether the specified [Port] is connected with this [Net].*/
    fun isConnectedWith(port: Port<out T>): Boolean

    /** Returns the [OutputPort] (if any) that asserts a consistent, defined signal to this [Net]. */
    fun getConsistentSignalPort(): OutputPort<T>?

    /**
     * Sets the current signal of this [Net] and forwards it to all connected [Port]s.
     * @param signal the signal to set.
     * @param origin the [OutputPort] that sends `signal` into this [Net].
     * @param signalHandler the runtime interface to the execution subsystem.
     * @param withDelay `true` if `signal` should be forwarded using a asynchronous scheduling step,
     *        `false` if `signal` should be forwarded immediately to the connected [Port]s.
     */
    fun setSignal(signal: T?, origin: OutputPort<T>, signalHandler: SignalHandler, withDelay: Boolean)

}

