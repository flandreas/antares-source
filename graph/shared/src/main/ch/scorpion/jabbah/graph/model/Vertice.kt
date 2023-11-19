package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.net.NetTopologyChangeEvent

/**
 * A [Vertice] is a node in a [Graph] that can be connected with other [Vertice]s using [Net]s that are attached
 * to the [Vertice]' [Port]s.
 *
 * The IDs of [Port]s in a [Vertice] start with 1, i.e. the first [Port] of a [Vertice] has ID 1.
 *
 * The description of this [Vertice] instance that can be customized by the user.
 * Note that this description is related to the instance, not the type. The description of the type is typically constant,
 * while two [Vertice]s of the same type can have distinctive instance descriptions, which can be used by the user
 * to be able to distinguish between them.
 *
 * This interface contains various generic methods for getting typed [Port]s to support the following usage pattern:
 * `val signal = vertice.getOutput<Boolean>().getOutgoingSignal()`
 * Since these methods are public API and access non-public state, they cannot be inlined and reified.
 * Implementation will therefore have no other choice than suppress or accept 'unchecked cast' warnings.
 */
interface Vertice : GraphElement, Describable {

	companion object {

		/** Used in [GraphElementEvent]s sent to [GraphElementListener], typically views.*/
		const val STATE_CHANGE_INPUT = "input"
		const val STATE_CHANGE_OUTPUT = "output"
		const val STATE_CHANGE_TYPE = "type"
	}

    /**
     * The name of this [Vertice]. Is often provided by the user and can serve to distinguish two [Vertice]s of the same type
     * It can also be empty. Example: "A".
     */
    var name: String?

    /** The overall number of [Port]s this [Vertice] contains, independent of the [PortType].*/
    val portsCount: Int

    /** The number of [InputPort]s of this [Vertice].*/
    val inputCount: Int

    /** The number of [OutputPort]s of this [Vertice].*/
    val outputCount: Int

    /** Determines whether any of this [Vertice]' [Port]s is connected to a [Net].*/
    val isConnected: Boolean

	/** Returns `true` if all [Port]s are connected to a [Net].*/
	val isFullyConnected: Boolean

    val hasAnyOutput: Boolean

	/** Determines whether all [Port.name] in this [Vertice] must be unique.*/
	val requireUniquePortNames: Boolean get() = true

    /**
     * Notifies this [Vertice] that one of its [InputPort]s has changed its signal.
     * Typical implementations will request the [SignalHandler] for re-execution after their propagation delay.
     * @param input the [InputPort] that received the incoming signal
     * @param force `true` if the signal should be used even if its the same as the currently available signal.
     */
    fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler, force: Boolean)

    /**
     * Notifies this [Vertice] that one of its [OutputPort]s has changed its signal.
     *
     * This is mainly relevant if this [Vertice] contains a sub [Graph] whose output signals are forwarded
     * to the [OutputPort]s of this [Vertice], in which case this [Vertice] should inform its listeners
     * that its state has changed.
     */
    fun outputChanged(output: OutputPort<*>, signalHandler: SignalHandler)

    /** Adds the specified [Port] to this [Vertice].*/
    fun <T: Any> addPort(port: Port<T>)

    /** Removes the specified [Port] from this [Vertice].*/
    fun removePort(port: Port<*>)

    /**
     * Returns the first [Port] of this [Vertice]. Convenience method for [Vertice]s that only contain a single [Port].
     * @throws NoSuchElementException if this [Vertice] doesn't contain a [Port]
     */
    fun <T: Any> getPort(): Port<T>

    /** Returns all [Port]s of this [Vertice].*/
    fun getPorts(): List<Port<*>>

    /**
     * Returns the [Port] with the specified name.
     * @throws NoSuchElementException if no [Port] with the specified name was found
     */
    fun <T: Any> getPort(name: String): Port<T>

	/** Determines whether this [Vertice] has a [Port] with the given name.*/
	fun hasPort(name: String?): Boolean

    /**
     * Returns the [Port] with the specified ID.
     * @param id the ID of the [Port], with 1 identifying the first [Port]
     * @throws NoSuchElementException if no [Port] with the specified ID was found
     */
    fun <T: Any> getPort(id: Int): Port<T>

    fun hasPort(id: Int): Boolean

    fun hasInput(name: String?): Boolean

    /**
     * Returns the first [InputPort] of this [Vertice]. Convenience method for [Vertice]s that only contain a single [InputPort].
     * @throws NoSuchElementException if this [Vertice] doesn't contain an [InputPort]
     */
    fun <T: Any> getInput(): InputPort<T>

    /** Returns all [InputPort]s of this [Vertice].*/
    fun getInputs(): List<InputPort<*>>

    /**
     * Returns the [InputPort] with the specified name.
     * @throws NoSuchElementException if no [InputPort] with the specified name was found
     */
    fun <T: Any> getInput(name: String): InputPort<T>

    /**
     * Returns the [InputPort] with the specified ID.
     * @param id the ID of the [Port], with 1 identifying the first [Port]
     * @throws NoSuchElementException if no [InputPort] with the specified ID was found
     */
    fun <T: Any> getInput(id: Int): InputPort<T>

    fun hasOutput(name: String?): Boolean

    /**
     * Returns the first [OutputPort] of this [Vertice]. Convenience method for [Vertice]s that only contain a single [OutputPort].
     * @throws NoSuchElementException if this [Vertice] doesn't contain an [OutputPort]
     */
    fun <T: Any> getOutput(): OutputPort<T>

    /** Returns all [OutputPort]s of this [Vertice].*/
    fun getOutputs(): List<OutputPort<*>>

    /**
     * Returns the [OutputPort] with the specified name.
     * @throws NoSuchElementException if no [OutputPort] with the specified name was found
     */
    fun <T: Any> getOutput(name: String): OutputPort<T>

    /**
     * Returns the [OutputPort] with the specified ID.
     * @param id the ID of the [Port], with 1 identifying the first [Port]
     * @throws NoSuchElementException if no [OutputPort] with the specified ID was found
     */
    fun <T: Any> getOutput(id: Int): OutputPort<T>

    fun <T: Any> replaceUndefinedOutput(signal: T?)

	/**
	 * Notifies this [Vertice] that one of its [OutputPort] was asked to resend its current
	 * outgoing signal in order to re-establish it on a [Net] due to a [NetTopologyChangeEvent].
	 * [Vertices][Vertice] that store the signal separately have a chance to update it accordingly.
	 */
	fun <T: Any> notifyResendSignal(port: OutputPort<T>, signalHandler: SignalHandler)
}