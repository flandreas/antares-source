package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of the [Scenarios] interface.
 */
class ScenariosImpl(
	override var graphView: GraphView? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) : Scenarios {

	private var isLoading: Boolean = false
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
		add(scenario, scenarios.size)
	}

	override fun add(scenario: Scenario, index: Int) {
		if (!isLoading) {
			scenario.id = getMaxId() + 1
		}
		scenarios.add(index, scenario)
		eventBus.post(ScenarioAddedEvent(graphView!!, scenario))
	}

	override fun remove(scenario: Scenario) {
		scenarios.remove(scenario)
		eventBus.post(ScenarioRemovedEvent(graphView!!, scenario))
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

	override fun moveStep(scenario: Scenario, step: ScenarioStep, index: Int) {
		scenario.moveStep(step, index)
		eventBus.post(ScenarioStepMovedEvent(graphView!!, scenario, step, index))
	}

	override fun indexOfScenario(scenario: Scenario): Int = scenarios.indexOf(scenario)

	override fun indexOfStep(scenario: Scenario, step: ScenarioStep): Int = scenario.indexOf(step)

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

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

	override fun getStorableChildren(): Iterator<Storable> {
		return scenarios.iterator()
	}

	/** ---- [ScenariosImpl] */

	private fun getMaxId(): Int {
		if (scenarios.size == 0) {
			return 0
		}
		return scenarios.maxBy { it.id }!!.id
	}
}