package io.antarescircuit.jabbah.graph.view.scenario

import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.view.*
import io.antarescircuit.jabbah.io.*

/**
 * A standard implementation of the [Scenarios] interface.
 */
class ScenariosImpl(
	graphView: GraphView? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractStorable(), Scenarios {

	private var isLoading: Boolean = false
	private val scenarios: MutableList<Scenario> by lazy { mutableListOf() }

	/** ---- [Scenarios] interface */

	override val isEmpty: Boolean get() = scenarios.isEmpty()

	override var graphView: GraphView? = graphView
		set(value) {
			field = value
			scenarios.forEach { it.graphView = value }
		}

	override fun dispose() {
		scenarios.forEach { it.dispose() }
		scenarios.clear()
	}

	override fun executionStart(graphView: GraphView, signalHandler: SignalHandler) {
		scenarios.forEach { it.executionStart(graphView, signalHandler) }
	}

	override fun getScenarios(): Iterable<Scenario> = scenarios.toImmutableList()

	override fun get(id: Int): Scenario = scenarios.first { it.id == id }

	override fun add(name: String) {
		add(ScenarioImpl(name))
	}

	override fun add(scenario: Scenario) {
		add(scenario, scenarios.size)
	}

	override fun add(scenario: Scenario, index: Int) {
		if (!isLoading) {
			scenario.id = getMaxId() + 1
		}
		scenario.graphView = graphView
		scenarios.add(index, scenario)
		eventBus.post(ScenarioAddedEvent(graphView!!, scenario))
	}

	override fun remove(scenario: Scenario) {
		scenario.graphView = null
		scenarios.remove(scenario)
		eventBus.post(ScenarioRemovedEvent(graphView!!, scenario))
	}

	override fun remove(scenarioId: Int) {
		remove(get(scenarioId))
	}

	override fun move(scenarioId: Int, index: Int) {
		val scenario = get(scenarioId)
		val oldIndex = indexOfScenario(scenarioId)
		val effIndex = if (oldIndex <= index) index - 1 else index
		scenarios.remove(scenario)
		scenarios.add(effIndex, scenario)
		eventBus.post(ScenarioMovedEvent(graphView!!, scenario, index))
	}

	override fun addStep(scenario: Scenario, step: ScenarioStep) {
		addStep(scenario, step, scenario.stepCount)
	}

	override fun addStep(scenario: Scenario, step: ScenarioStep, index: Int) {
		scenario.addStep(step, index)
		eventBus.post(ScenarioStepAddedEvent(graphView!!, scenario, step))
	}

	override fun removeStep(scenario: Scenario, step: ScenarioStep) {
		scenario.removeStep(step)
		eventBus.post(ScenarioStepRemovedEvent(graphView!!, scenario, step))
	}

	override fun removeStep(scenarioId: Int, stepId: Int) {
		val scenario = get(scenarioId)
		removeStep(scenario, scenario.getStep(stepId))
	}

	override fun moveStep(scenarioId: Int, stepId: Int, index: Int) {
		val scenario = get(scenarioId)
		val step = scenario.getStep(stepId)
		scenario.moveStep(step, index)
		eventBus.post(ScenarioStepMovedEvent(graphView!!, scenario, step, index))
	}

	override fun indexOfScenario(scenario: Scenario): Int = scenarios.indexOf(scenario)

	override fun indexOfScenario(scenarioId: Int): Int = indexOfScenario(get(scenarioId))

	override fun indexOfStep(scenario: Scenario, step: ScenarioStep): Int = scenario.indexOf(step)

	override fun indexOfStep(scenarioId: Int, scenarioStepId: Int): Int {
		val scenario = get(scenarioId)
		return indexOfStep(scenario, scenario.getStep(scenarioStepId))
	}

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeStorables("scenarios", scenarios.iterator())
	}

    override fun read(reader: StoreReader) {
	    try {
		    isLoading = true
	        scenarios.addAll(reader.readStorables("scenarios"))
	    } finally {
	    	isLoading = false
	    }
	}

	/** ---- [ScenariosImpl] */

	private fun getMaxId(): Int {
		if (scenarios.size == 0) {
			return 0
		}
		return scenarios.maxByOrNull { it.id }!!.id
	}
}