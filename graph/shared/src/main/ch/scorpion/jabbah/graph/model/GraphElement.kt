package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorState
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.io.Storable

/**
 * Represents an element of a [Graph].
 */
interface GraphElement : Storable, Actor, Describable {

    /** Holds the identification of this [GraphElement] being unique within a [Graph]. */
    override var id: Int

	/**
	 * Returns a short translated type name of this [GraphElement].
	 *
	 * The type of a [GraphElement] names the "kind" or the nature of a [GraphElement].
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
}

/** An event sent by a [GraphElement] whenever its state has changed. */
class GraphElementEvent(
	val element: GraphElement,
	val signalHandler: SignalHandler? = null,
	val reason: String? = null
)

/** Listens for [GraphElementEvent]s from [GraphElement]s.*/
interface GraphElementListener {
    fun stateChanged(e: GraphElementEvent)
}

open class GraphElementAdapter : GraphElementListener {
    override fun stateChanged(e: GraphElementEvent) { }
}
