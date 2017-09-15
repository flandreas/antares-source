package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Snapper
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.Storable

/**
 * A [Drawing] that consists of [GraphElementView]s.
 *
 * When [GraphElementView]s are added to or removed from a [GraphView], the underlying [Graph] is
 * automatically updated with the [GraphElement] of the added or removed [GraphElementView].
 *
 * Disposing a [GraphView] detaches it and all contained [GraphElementView]s from their models,
 * and releases held resources.
 *
 * @param T the type of [GraphElementView] that this [GraphView] contains and displays.
 */
interface GraphView<T : GraphElementView<*>> : Drawing<T> {

    /** The [Graph] that this [GraphView] displays. Only `null` during deserialization.*/
    var graph: Graph?

    /**
     * Holds the [Snapper] to be used by all automatic layout activity being done on [GraphElementView]s
     * displayed by this {@link GraphView}.
     */
    var snapper: Snapper?

    /** Returns the [Scenarios] that are defined for this [GraphView].*/
    val scenarios: Scenarios

    /** The current [Scenario] of this [GraphView], if any. Posts a [ScenarioEvent] if changed.*/
    var currentScenario: Scenario?

    /** The current [ScenarioStep] of this [GraphView], if any. Posts a [ScenarioStepEvent] if changed.*/
    var currentScenarioStep: ScenarioStep?

    /**
     * Asks this [GraphView] to make sure that all its [GraphElementView]s are properly bound to their models.
     *
     * This method is automatically called typically before simulation is started and is needed because [GraphView]s
     * can contain [SubGraphVerticeView]s that might depend on model information of the referenced sub
     * [SubGraph], which is not available before [Graph] binding. Note however that this method should **not** bind
     * [Graph] models in terms of [Graph.bind]; this is the responsibility of other classes.
     */
    fun bind()

    /**
     * Creates a clone of this [GraphView] that is connected with another model instance, and **not**
     * with the [Graph] of this [GraphView].
     *
     * @param model the [Graph] with which the created clone is connected. Must have the same structure like this
     *      [GraphView]'s model.
     *
     * @param storableCreator the [StorableCreator] to be used for creating the [Storable]s, or nothing
     *      if the default [StorableCreator] is to be used.
     * @return a clone of this [GraphView] that is connected with [model]
     */
    fun cloneForExistingModel(model: Graph, storableCreator: StorableCreator = IOModule.storableCreator): GraphView<T>

    /** Returns all [EdgeViews] that this [GraphView] contains.*/
    fun getEdgeViews(): ImmutableList<EdgeView<Any>>

    /** Returns the [EdgeView] that is connected with the specified [Port], if any.*/
    fun getEdgeView(port: Port<*>): EdgeView<Any>?

    /** Returns all [GraphPortView]s that this [GraphView] contains.*/
    fun getGraphPortViews(): ImmutableList<GraphPortView<GraphPort<Any>>>

    /** Returns the [GraphPortView] of the [GraphPort] with the specified name, if any.*/
    fun getGraphPortView(portName: String): GraphPortView<GraphPort<Any>>?

    fun getControlViewSources(): ImmutableList<ControlViewSource<Vertice>>

    /** Returns the [ControlViewSource] with the specified ID, if any.*/
    fun getControlViewSource(controlId: String): ControlViewSource<Vertice>?

    /** Returns all [GraphElementView]s of a particular [GraphElement].*/
    fun getElementViews(element: GraphElement): ImmutableList<GraphElementView<*>>

    // TODO Add Scenario methods
}

/**
 * Posted on the [EventBus] of a [GraphView] when its [OscilloscopeView] changes its visibility.
 * TODO Delete, not needed any more
 */
data class OscilloscopeDisplayedEvent(val graphView: GraphView<*>)
