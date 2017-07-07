package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.io.Storable

/**
 * Encapsulates the {@link Scenario}s of a {@link GraphView} and the corresponding management methods into
 * a single object.
 */
interface Scenarios : Storable {

    /** Can be `null` in order to be instantiated by deserialization .*/
    var graphView: GraphView<*>?

    val isEmpty: Boolean

    fun dispose()

    fun getScenarios(): Iterable<Scenario>

    fun get(id: Int): Scenario

    fun add(name: String)

    fun add(scenario: Scenario)

    fun remove(scenario: Scenario)

    fun addStep(scenario: Scenario, step: ScenarioStep)

    fun removeStep(scenario: Scenario, step: ScenarioStep)
}

data class ScenarioAddedEvent(val graphView: GraphView<*>, val scenario: Scenario)
data class ScenarioRemovedEvent(val graphView: GraphView<*>, val scenario: Scenario)

data class ScenarioStepAddedEvent(val graphView: GraphView<*>, val scenario: Scenario, val scenarioStep: ScenarioStep)
data class ScenarioStepRemovedEvent(val graphView: GraphView<*>, val scenario: Scenario, val scenarioStep: ScenarioStep)