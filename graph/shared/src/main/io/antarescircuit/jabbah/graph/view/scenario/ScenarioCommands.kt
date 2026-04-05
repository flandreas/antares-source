package io.antarescircuit.jabbah.graph.view.scenario

import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.UndoableDataHolder
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Scenario
import io.antarescircuit.jabbah.graph.view.ScenarioStep
import io.antarescircuit.jabbah.io.StorableCloner

/**
 * Adds a clone of a [Scenario] to a [GraphView].
 */
class AddScenarioCommand(
	private val dataHolder: UndoableDataHolder,
	private val scenario: Scenario
) : AbstractCommand("scenario.command.scenario.add", null), Undoable {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView
	var addedScenarioId: Int = 0
		private set

	override fun execute() {
		val clone = StorableCloner.clone(scenario)
		graphView.scenarios.add(clone)
		addedScenarioId = clone.id
	}

	override fun undo() {
		graphView.scenarios.remove(addedScenarioId)
	}
}

/**
 * Deletes a [Scenario] from a [GraphView].
 * By intention not [Undoable] to avoid the need to store a clone.
 */
class DeleteScenarioCommand(
	private val dataHolder: UndoableDataHolder,
	private val scenarioId: Int
) : AbstractCommand("scenario.command.scenario.delete", null) {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView

	override fun execute() {
		graphView.scenarios.remove(scenarioId)
	}
}

/** Adds a [ScenarioStep] to a [Scenario].*/
class AddScenarioStepCommand(
	private val dataHolder: UndoableDataHolder,
	private val scenarioId: Int,
	private val scenarioStep: ScenarioStep
) : AbstractCommand("scenario.command.scenarioStep.add"), Undoable {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView
	var addedScenarioStepId: Int = 0
		private set

	override fun execute() {
		val clone = StorableCloner.clone(scenarioStep)
		graphView.scenarios.addStep(graphView.scenarios.get(scenarioId), clone)
		addedScenarioStepId = clone.id
	}

	override fun undo() {
		graphView.scenarios.removeStep(scenarioId, addedScenarioStepId)
	}
}

/**
 * Deletes a [ScenarioStep] from its [Scenario].
 * By intention not [Undoable] to avoid the need to store a clone.
 */
class DeleteScenarioStepCommand(
	private val dataHolder: UndoableDataHolder,
	private val scenarioId: Int,
	private val scenarioStepId: Int
) : AbstractCommand("scenario.command.scenarioStep.delete") {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView

	override fun execute() {
		graphView.scenarios.removeStep(scenarioId, scenarioStepId)
	}
}

/** Moves a [ScenarioStep] within its [Scenario], i.e. changes the position in the ordered list.*/
class MoveScenarioStepCommand(
	private val dataHolder: UndoableDataHolder,
	private val scenarioId: Int,
	private val scenarioStepId: Int,
	private val newIndex: Int
) : AbstractCommand("scenario.command.scenarioStep.move"), Undoable {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView
	private val oldIndex: Int = graphView.scenarios.indexOfStep(scenarioId, scenarioStepId)

	override fun execute() {
		graphView.scenarios.moveStep(scenarioId, scenarioStepId, newIndex)
	}

	override fun undo() {
		graphView.scenarios.moveStep(scenarioId, scenarioStepId, oldIndex)
	}
}

class MoveScenarioCommand(
	private val dataHolder: UndoableDataHolder,
	private val scenarioId: Int,
	private val newIndex: Int
) : AbstractCommand("scenario.command.scenario.move"), Undoable {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView
	private val oldIndex: Int = graphView.scenarios.indexOfScenario(scenarioId)

	override fun execute() {
		graphView.scenarios.move(scenarioId, newIndex)
	}

	override fun undo() {
		val effIndex = if (oldIndex > newIndex) oldIndex + 1 else oldIndex
		graphView.scenarios.move(scenarioId, effIndex)
	}
}
