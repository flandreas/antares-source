package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of the [Scenarios] interface.
 */
class ScenariosImpl(override var graphView: GraphView<*>?, val eventBus: EventBus) : Scenarios {
    constructor(): this(null, BaseModule.eventBus)

    private val scenarios: MutableList<Scenario> by lazy { mutableListOf<Scenario>() }

    /** ---- [Scenarios] interface */

    override val isEmpty: Boolean get() = scenarios.isEmpty()

    override fun dispose() {
        scenarios.forEach { it.dispose() }
        scenarios.clear()
    }

    override fun getScenarios(): Iterable<Scenario> {
        return scenarios.toImmutableList()
    }

    override fun get(id: Int): Scenario {
        return scenarios.first { it.id == id }
    }

    override fun add(name: String) {
        add(ScenarioImpl(name))
    }

    override fun add(scenario: Scenario) {
        // TODO Ensure name uniqueness
        scenario.id = scenarios.size + 1
        scenarios.add(scenario)
        eventBus.post(ScenarioAddedEvent(graphView!!, scenario))
    }

    override fun remove(scenario: Scenario) {
        scenarios.remove(scenario)
        eventBus.post(ScenarioRemovedEvent(graphView!!, scenario))
    }

    override fun addStep(scenario: Scenario, step: ScenarioStep) {
        scenario.addStep(step)
        eventBus.post(ScenarioStepAddedEvent(graphView!!, scenario, step))
    }

    override fun removeStep(scenario: Scenario, step: ScenarioStep) {
        scenario.removeStep(step)
        eventBus.post(ScenarioStepRemovedEvent(graphView!!, scenario, step))
    }

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        // empty
    }

    override fun write(writer: StoreWriter) {
        writer.writeStorables("scenarios", scenarios.iterator())
    }

    override fun read(reader: StoreReader) {
        scenarios.addAll(reader.readStorables("scenarios") as Collection<Scenario>)
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return scenarios.iterator()
    }
}