package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator

/**
 * Represents an element of a [Graph].
 */
interface GraphElement : Storable, Actor {

    /** Holds the identification of this [GraphElement] being unique within a [Graph]. */
    override var id: Int

    /**
     * Determines whether this [GraphElement] is currently in an error state, which can be caused by either a
     * static [DesignError] or a dynamic [ExecutionError].
     * @return `true` if this [GraphElement] is currently in an error state
     */
    val isError: Boolean

    /** Holds the current [ExecutionError] of this [GraphElement], if any. */
    var executionError: ExecutionError?

    /** Holds the current [DesignError] of this [GraphElement], if any. */
    val designError: DesignError?

    fun accept(visitor: HierarchyVisitor): Boolean

    /** Adds the specified [GraphElementListener] to this [GraphElement]. */
    fun addGraphElementListener(l: GraphElementListener)

    /** Removes the specified [GraphElementListener] from this [GraphElement]. */
    fun removeGraphElementListener(l: GraphElementListener)

    /**
     * Asks this [GraphElement] to bind itself with referenced sub [Graph]s, if applicable.
     * @param repository the [MetaGraphRepository] from which sub [Graph]s are retrieved
     * @param storableCreator the [StorableCreator] to be used when cloning [Graph]s from the [MetaGraphRepository].
     */
    fun bind(repository: MetaGraphRepository, storableCreator: StorableCreator)
}

/** An event sent by a [GraphElement] whenever its state has changed. */
class GraphElementEvent(val element: GraphElement, val signalHandler: SignalHandler? = null)

/** Listens for [GraphElementEvent]s from [GraphElement]s.*/
interface GraphElementListener {
    fun stateChanged(e: GraphElementEvent)
    fun executionStarted(signalHandler: SignalHandler)
    fun executionStopped(signalHandler: SignalHandler)
}

open class GraphElementAdapter : GraphElementListener {
    override fun stateChanged(e: GraphElementEvent) { }
    override fun executionStarted(signalHandler: SignalHandler) { }
    override fun executionStopped(signalHandler: SignalHandler) { }
}
