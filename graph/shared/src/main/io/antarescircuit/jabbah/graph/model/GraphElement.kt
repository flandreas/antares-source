package io.antarescircuit.jabbah.graph.model

import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.execution.ExecutionError
import io.antarescircuit.jabbah.base.event.VetoException
import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.edit.model.text.description.Describable
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorState
import io.antarescircuit.jabbah.graph.MetaGraphRepository
import io.antarescircuit.jabbah.graph.model.net.CombinedNet
import io.antarescircuit.jabbah.graph.model.nonvolatile.NonVolatileStorable
import io.antarescircuit.jabbah.graph.model.param.GraphParamValues
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.io.Storable

/**
 * Represents an element of a [Graph].
 */
interface GraphElement : Storable, Actor, Describable {

    /** Holds the identification of this [GraphElement] being unique within a [Graph]. */
    override var id: Int

	/**
	 * Returns a short translated type name of this [GraphElement].
	 *
	 * The type of [GraphElement] names the "kind" or the nature of a [GraphElement].
	 * Typically, the type is not persistent, but provided by concrete implementation of the [GraphElement] interface.
	 *
	 * Example: "AND Gate"
	 */
	val type: String

	/**
	 * Returns a longer translated description of [type].
	 *
	 * Example: "An AND gate is a component that performs a logical "AND" operation on all input values."
	 */
	val typeDesc: String?

	/**
     * Determines whether this [GraphElement] is currently in an error state, which can be caused by either a
     * static [DesignError] or a dynamic [ExecutionError].
     * @return `true` if this [GraphElement] is currently in an error state
     */
    val isError: Boolean

    /** Holds the current [DesignError] of this [GraphElement], if any. */
    val designError: DesignError?

	/**
	 * A [GraphElement] is considered inactive if it is part of [Graph] that is being executed
	 * using "flat execution" and that is contained in a [SubGraphVerticeRef] whose logic is defined by a script.
	 * Control views of such [GraphElement]s should be rendered in an inactive state in order not to confuse the user.
	 */
	val inactive: Boolean get() = state == ActorState.NonExecuting

    fun accept(visitor: HierarchyVisitor): Boolean

    /** Adds the specified [GraphElementListener] to this [GraphElement]. */
    fun addGraphElementListener(l: GraphElementListener)

    /** Removes the specified [GraphElementListener] from this [GraphElement]. */
    fun removeGraphElementListener(l: GraphElementListener)

    /**
     * Asks this [GraphElement] to bind itself with referenced sub [Graph]s, if applicable.
     * @param repository the [MetaGraphRepository] from which sub [Graph]s are retrieved
     */
    fun bind(deep: Boolean, repository: MetaGraphRepository)

	/** Forms the necessary [CombinedNet]s used for execution.*/
	fun formNet(signalHandler: SignalHandler)

	/** Called by services that updated this [GraphElement], which then informs all registered [GraphElementListener].*/
	fun notifyStateChanged()

	/**
	 * Called by the specified [Graph] when its [GraphParamValues] have changed.
	 * A [Vertice] containing properties that depend on [GraphParamValues] should react accordingly.
	 * @throws DslError if application of [GraphParamValues] to properties with expressions fails
	 */
	fun graphParamsChanged(graph: Graph)

	/**
	 * Called by the copy/paste system after this [GraphElement] was created as a result of a paste
	 * operation (indirectly via its view), but before it is added to the destination [Graph].
	 * This gives this [GraphElement] a chance to adjust any of its properties, e.g. changing
	 * its name "Hello" to "Hello (2)".
	 */
	fun beforePaste(graph: Graph) {}

	/**
	 * Complements [Actor.executionInitialize] in cases where this [GraphElement] is given the
	 * opportunity to load [NonVolatileStorable] data from previous execution runs.
	 * Cannot use [executionInitialize] from the execution module because it doesn't depend on io module.
	 * The default implementation simply calls [executionInitialize].
	 */
	fun executionInitializeNonVolatile(signalHandler: SignalHandler, nonVolatileData: NonVolatileStorable? = null) {
		executionInitialize(signalHandler)
	}

	/**
	 * Complements [Actor.executionStopped] in cases where this [GraphElement] is given the
	 * opportunity to store [NonVolatileStorable] data to make is available in successive execution runs.
	 * Cannot use [executionStopped]  from the execution module because it doesn't depend on io module.
	 * The default implementation simply calls [executionStopped].
	 */
	fun executionStoppedNonVolatile(signalHandler: SignalHandler, nonVolatileData: NonVolatileStorable? = null) {
		executionStopped(signalHandler)
	}
}

/** An event sent by a [GraphElement] whenever its state has changed. */
open class GraphElementEvent(
	val element: GraphElement,
	val signalHandler: SignalHandler? = null,
	val reason: String? = null,
	val argument: Any? = null
)

/** Listens for [GraphElementEvent]s from [GraphElement]s.*/
interface GraphElementListener {

	/**
	 * Called for vetoable [GraphElementEvent]s. Implementations should check whether they would
	 * accept [e], in which case the do nothing, otherwise throw a [VetoException].
	 * If no [GraphElementListener] has vetoed the change, each of them is called with [stateChanged].
	 */
	fun checkStateChange(e: GraphElementEvent)

	/** Called by [GraphElement]s when its state has changed.*/
    fun stateChanged(e: GraphElementEvent)
}

/** Base class providing default (empty) implementations for [GraphElementListener].*/
open class GraphElementAdapter : GraphElementListener {
	override fun stateChanged(e: GraphElementEvent) {}
	override fun checkStateChange(e: GraphElementEvent) {}
}

