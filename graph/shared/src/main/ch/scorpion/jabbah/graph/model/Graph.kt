package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraphRepository

/**
 * A [Graph] is mainly a collection of [GraphElement]s.
 *
 * The propagation delay property of a [Graph] is only used for informative purposes. The time it takes until a
 * changed value arrives at a [GraphOutput] is determined by the inner structure of the [Graph], i.e. by the
 * propagation delays of the inner [GraphElement]s and the way they are connected. A [Graph] implementation
 * is not supposed to try to calculate the overall propagation from the contained [GraphElement]'s propagation
 * delays, which is anyway impossible due to feedback loop. However, the designer can manually specify the overall
 * propagation delay of a [Graph], which helps the consumer of a [Graph] to design the timing aspects of a
 * surrounding [Graph] that uses this [Graph], such as to derive a proper clock design for synchronous
 * applications.
 */
interface Graph : Storable {

    /** The universal unique ID of this [Graph]. Used for referencing this [Graph] from other [Graph]s.*/
    val uuid: UUID

    /**
     * The overall estimated propagation delay of this [Graph] in nanoseconds, i.e. the estimated time it
     * takes to calculate new output values when an input has changed.
     */
    var propagationDelay: Long?

    /**
     * The name of this [Graph] to be used when offering this [Graph] in a [Library].
     * Posts a [GraphNameChangedEvent] on this [Graph]'s [EventBus] when changed.
     */
    var name: String

    /** A short description of the purpose of this [Graph].*/
    var shortDescription: String?

    /** The script code to be executed when a [GraphInput] has changed and deep execution is not required.*/
    var script: String?

    /** Returns the number of [GraphElement]s of this [Graph].*/
    val elementsCount: Int

    /** Returns the [GraphElement]s of this [Graph] as an immutable list.*/
    val elements: ImmutableList<GraphElement>

    /** Returns the [GraphInput]s (excluding [BidirectionalPort]s) of this [Graph] as an immutable list.*/
    val graphInputs: ImmutableList<GraphInput<*>>

    /** Returns the [GraphOutput]s (excluding [BidirectionalPort]s) of this [Graph] as an immutable list.*/
    val graphOutputs: ImmutableList<GraphOutput<*>>

    /** Returns the [BidirectionalGraphPort]s of this [Graph] as an immutable list.*/
    val graphInOuts: ImmutableList<BidirectionalGraphPort<*>>

    /** Returns the [GraphPort]s of this [Graph] as an immutable list.*/
    val graphPorts: ImmutableList<GraphPort<*>>

    fun accept(visitor: HierarchyVisitor): Boolean

	/**
	 * Initializes the property [uuid] of this [Graph] by creating a new one.
	 * This is only needed when creating a copy or clone of this [Graph] that needs to receive its own identity.
	 */
	fun initializeUUID()

    /**
     * Adds the specified [GraphElement] to this [Graph].
     * Posts a [GrapElementAddedEvent] on this [Graph]'s [EventBus].
     * @return this [Graph] to support method chaining
     */
    fun add(graphElement: GraphElement): Graph

    /**
     * Removes the specified [GraphElement] from this [Graph].
     * Posts a [GrapElementRemovedEvent] on this [Graph]'s [EventBus].
     * @return this [Graph] to support method chaining
     */
    fun remove(graphElement: GraphElement): Graph

    /** Removes all [GraphElement]s from this [Graph].*/
    fun clear(): Graph

    /** Determines whether this [Graph] contains the specified [GraphElement].*/
    fun contains(graphElement: GraphElement): Boolean

    /** Returns the [GraphElement] with the specified ID.*/
    fun withId(id: Int): GraphElement?

    /** Returns the [GraphElement] with the specified [Storable] ID.*/
    fun withStorableId(storableId: Int): GraphElement?

    /** Called by the execution environment after the execution has been started.*/
    fun executionStarted(signalHandler: SignalHandler)

    /** Called by the execution environment after the execution has been stopped.*/
    fun executionStopped(signalHandler: SignalHandler)

    /** Binds all [GraphElement]s of this [Graph] using the specified [MetaGraphRepository].*/
    fun bind(repository: MetaGraphRepository, storableCreator: StorableCreator)

    /** Returns the [GraphPort] with the specified name.*/
    fun <T: Any> getGraphPort(name: String): GraphPort<T>?

    /** Returns the [GraphInput] or the [BidirectionalPort] with the specified name.*/
    fun <T: Any> getGraphInput(name: String): GraphInput<T>?

    /** Returns the [GraphOutput] or the [BidirectionalPort] with the specified name.*/
    fun <T: Any> getGraphOutput(name: String): GraphOutput<T>?

}

class GraphNameChangedEvent(val graph: Graph, val oldName: String, val newName: String)
class GraphElementAddedEvent(val graph: Graph, val element: GraphElement)
class GraphElementRemovedEvent(val graph: Graph, val element: GraphElement)