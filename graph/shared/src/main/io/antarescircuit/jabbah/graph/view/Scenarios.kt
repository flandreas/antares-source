package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.execution.SignalHandler

/**
 * Encapsulates the [Scenario]s of a [GraphView] and the corresponding management methods
 * into a single object.
 */
interface Scenarios : Storable {

	var graphView: GraphView?

	val isEmpty: Boolean

	fun dispose()

	fun executionStart(graphView: GraphView, signalHandler: SignalHandler)

	fun getScenarios(): Iterable<Scenario>

	fun get(id: Int): Scenario

	/**
	 * Adds a new [Scenario] with the specified name as the last one in this [Scenarios].
	 * Posts a [ScenarioAddedEvent] on this [Scenarios]' [EventBus].
	 */
	fun add(name: String)

	/**
	 * Adds the specified [Scenario] as the last one in this [Scenarios].
	 * Posts a [ScenarioAddedEvent] on this [Scenarios]' [EventBus].
	 */
	fun add(scenario: Scenario)

	/**
	 * Adds the specified [Scenario] at the specified index to this [Scenarios].
	 * Posts a [ScenarioAddedEvent] on this [Scenarios]' [EventBus].
	 */
	fun add(scenario: Scenario, index: Int)

	fun remove(scenario: Scenario)

	fun remove(scenarioId: Int)

	fun move(scenarioId: Int, index: Int)

	fun addStep(scenario: Scenario, step: ScenarioStep)

	fun addStep(scenario: Scenario, step: ScenarioStep, index: Int)

	fun removeStep(scenario: Scenario, step: ScenarioStep)

	fun removeStep(scenarioId: Int, stepId: Int)

	fun moveStep(scenarioId: Int, stepId: Int, index: Int)

	fun indexOfScenario(scenario: Scenario): Int

	fun indexOfScenario(scenarioId: Int): Int

	fun indexOfStep(scenario: Scenario, step: ScenarioStep): Int

	fun indexOfStep(scenarioId: Int, scenarioStepId: Int): Int
}

data class ScenarioAddedEvent(val graphView: GraphView, val scenario: Scenario)
data class ScenarioRemovedEvent(val graphView: GraphView, val scenario: Scenario)
data class ScenarioMovedEvent(val graphView: GraphView, val scenario: Scenario, val index: Int)

data class ScenarioStepAddedEvent(val graphView: GraphView, val scenario: Scenario, val scenarioStep: ScenarioStep)
data class ScenarioStepRemovedEvent(val graphView: GraphView, val scenario: Scenario, val scenarioStep: ScenarioStep)
data class ScenarioStepMovedEvent(val graphView: GraphView, val scenario: Scenario, val scenarioStep: ScenarioStep, val index: Int)