package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.edit.model.text.description.Describable

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
 */
interface Vertice : GraphElement, Describable {

    /**
     * The name of this [Vertice]. Is often provided by the user and can serve to distinguish two [Vertice]s of the same type
     * It can also be empty. Example: "A".
     */
    var name: String?

	val baseResourceKey: String

    /** The overall number of [Port]s this [Vertice] contains, independent of the [PortType].*/
    val portsCount: Int

    /** The number of [InputPort]s of this [Vertice].*/
    val inputCount: Int

    /** The number of [OutputPort]s of this [Vertice].*/
    val outputCount: Int

    /** Determines whether any of this [Vertice]' [Port]s is connected to a [Net].*/
    val isConnected: Boolean

    /** Notifies this [Vertice] that one of its [InputPort]s has changed its signal.*/
    fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler)

    /**
     * Notifies this [Vertice] that one of its [OutputPort]s has changed its signal.
     *
     * This is mainly relevant if this [Vertice] contains a sub {@link Graph} whose output signals are forwarded
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
    fun getPorts(): ImmutableList<Port<*>>

    /**
     * Returns the [Port] with the specified name.
     * @throws NoSuchElementException if no [Port] with the specified name was found
     */
    fun <T: Any> getPort(name: String): Port<T>

    /**
     * Returns the [Port] with the specified ID.
     * @param id the ID of the [Port], with 1 identifying the first [Port]
     * @throws NoSuchElementException if no [Port] with the specified ID was found
     */
    fun <T: Any> getPort(id: Int): Port<T>

    /**
     * Returns the first [InputPort] of this [Vertice]. Convenience method for [Vertice]s that only contain a single [InputPort].
     * @throws NoSuchElementException if this [Vertice] doesn't contain an [InputPort]
     */
    fun <T: Any> getInput(): InputPort<T>

    /** Returns all [InputPort]s of this [Vertice].*/
    fun getInputs(): ImmutableList<InputPort<*>>

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

    /**
     * Returns the first [OutputPort] of this [Vertice]. Convenience method for [Vertice]s that only contain a single [OutputPort].
     * @throws NoSuchElementException if this [Vertice] doesn't contain an [OutputPort]
     */
    fun <T: Any> getOutput(): OutputPort<T>

    /** Returns all [OutputPort]s of this [Vertice].*/
    fun getOutputs(): ImmutableList<OutputPort<*>>

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
}