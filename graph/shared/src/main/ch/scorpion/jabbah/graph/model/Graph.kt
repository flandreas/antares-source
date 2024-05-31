package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Issue
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.dsl.DslParser
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.Symbol
import ch.scorpion.jabbah.base.dsl.SymbolTable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.parser.Parser
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.Storable

interface GraphFactory {
	fun create(name: TranslatableText, type: GraphType): Graph
}

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
interface Graph : GraphPortOwner, Namable, Describable, Storable, Bean {

	val type: GraphType

    /** The universal unique ID of this [Graph]. Used for referencing this [Graph] from other [Graph]s.*/
    var uuid: UUID

    /**
     * The overall estimated propagation delay of this [Graph] in nanoseconds, i.e. the estimated time it
     * takes to calculate new output values when an input has changed.
     */
    var overallPropagationDelay: Long?

	/**
	 * The optional time (in nanoseconds) until this [Graph] has reached a stable state after starting execution.
	 * Used to skip signal flow animations during the start-up phase.
	 */
	var startupTime: Long?

    /** The script code to be executed when a [GraphInput] has changed and deep execution is not required.*/
    var script: String?

    /** If 'true', [script] is always used for execution, even if execution mode is deep.*/
    var purelyScripted: Boolean

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

	/**
	 * Returns a [SymbolTable] containing the name of all [GraphPort] and [GraphParamDefinitions]
	 * of this [Graph] as variable definitions.
	 */
	val symbolTable: SymbolTable

	/** Non-persistent definitions of parameters of this [Graph]. Made persistent by higher-level object.*/
	var parameterDefinitions: GraphParamDefinitions

	/** Non-persistent values for [parameterDefinitions]. */
	var parameterValues: GraphParamValues

	/** Informs this [Graph] that it is not actively used any more.*/
	fun dispose()

    fun accept(visitor: HierarchyVisitor): Boolean

    /**
     * Adds the specified [GraphElement] to this [Graph].
     * Posts a [GraphElementAddedEvent] on this [Graph]'s [EventBus].
     * @return this [Graph] to support method chaining
     */
    fun add(graphElement: GraphElement): Graph

    /**
     * Removes the specified [GraphElement] from this [Graph].
     * Posts a [GraphElementRemovedEvent] on this [Graph]'s [EventBus].
     * @return this [Graph] to support method chaining
     */
    fun remove(graphElement: GraphElement): Graph

    /** Removes all [GraphElement]s from this [Graph].*/
    fun clear(): Graph

    /** Determines whether this [Graph] contains the specified [GraphElement].*/
    fun contains(graphElement: GraphElement): Boolean

    /** Returns the [GraphElement] with the specified ID.*/
    fun withId(id: Int): GraphElement?

	/** Binds all [GraphElement]s of this [Graph] using the specified [MetaGraphRepository].*/
	fun bind(deep: Boolean, repository: MetaGraphRepository)

	/**
	 * Forms the [CombinedNet]s to be used during execution.
	 * Must be called before [executionStart] to avoid race conditions for the established [CombinedNet]s.
	 */
	fun formNet(signalHandler: SignalHandler)

	/**
	 * Checks for design errors in this [Graph] and posts an [Issue] on the specified [EventBus] for every
	 * detected error. This is called before execution of this [Graph] is started. Design errors are defined
	 * in terms of [GraphElement.designError].
	 * @return `true` if the design is okay, `false` if a design error has been found
	 */
	fun checkDesign(signalHandler: SignalHandler, eventBus: EventBus): Boolean

	fun executionInitialize(signalHandler: SignalHandler)

	/**
	 * Called by the execution environment after the execution has been started.
	 * Some types of [Graph] depend on the topology in the [GraphView] for execution.
	 */
    fun executionStart(signalHandler: SignalHandler, graphView: GraphView?)

    /** Called by the execution environment after the execution has been stopped.*/
    fun executionStopped(signalHandler: SignalHandler)

    /** Returns the [GraphInput] or the [BidirectionalPort] with the specified name.*/
    fun <T: Any> getGraphInput(name: String): GraphInput<T>?

    /** Returns the [GraphOutput] or the [BidirectionalPort] with the specified name.*/
    fun <T: Any> getGraphOutput(name: String): GraphOutput<T>?

	/**
	 * Creates a [DslParser] for parsing the [Graph]'s execution script.
	 * The created [DslParser] contains a [SemanticAnalyser] that uses this [GraphView] as
	 * context [SymbolTable] with all [GraphPort]s predefined as [Symbol]s.
	 */
	fun createParser(program: String, semanticAnalyser: SemanticAnalyser?): Parser

}

class GraphElementAddedEvent(val graph: Graph, val element: GraphElement)

class GraphElementRemovedEvent(val graph: Graph, val element: GraphElement)