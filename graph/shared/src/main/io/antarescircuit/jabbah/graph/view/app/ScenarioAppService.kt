package io.antarescircuit.jabbah.graph.view.app

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.UndoableDataHolder
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Scenario
import io.antarescircuit.jabbah.graph.view.ScenarioStep
import io.antarescircuit.jabbah.graph.view.scenario.*

class ScenarioAppService(
	private val commandManager: CommandManager = EditModule.commandManager
) {

	companion object {
		private val LOG by logger(ScenarioAppService::class)
	}

	private fun graphView(dataHolder: UndoableDataHolder): GraphView = (dataHolder.getUndoableState() as MetaGraph).graph.graphView

	/**
	 * Creates a clone of [scenario] and adds it to the [GraphView] in the [ApplicationData] of [application].
	 * @return the ID of the cloned [Scenario]
	 */
	fun addScenario(dataHolder: UndoableDataHolder, scenario: Scenario): Int {
		LOG.trace("Add new Scenario to GraphView ${graphView(dataHolder).graph?.uuid}")
		val command = AddScenarioCommand(dataHolder, scenario)
		commandManager.execute(command)
		return command.addedScenarioId
	}

	/** Deletes the [Scenario] with the specified ID from the [GraphView] in the [ApplicationData] of [application]. */
	fun deleteScenario(dataHolder: UndoableDataHolder, scenarioId: Int) {
		LOG.trace("Delete Scenario $scenarioId from GraphView ${graphView(dataHolder).graph?.uuid}")
		commandManager.execute(DeleteScenarioCommand(dataHolder, scenarioId))
	}

	/**
	 * Creates a clone of [scenarioStep] and adds it to the specified [Scenario] in the [ApplicationData]
	 * of [application].
	 */
	fun addScenarioStep(dataHolder: UndoableDataHolder, scenarioId: Int, scenarioStep: ScenarioStep): Int {
		LOG.trace("Add new ScenarioStep to Scenario $scenarioId in GraphView ${graphView(dataHolder).graph?.uuid}")
		val command = AddScenarioStepCommand(dataHolder, scenarioId, scenarioStep)
		commandManager.execute(command)
		return command.addedScenarioStepId
	}

	/**
	 * Deleted the [ScenarioStep] with ID [scenarioStepId] from the [Scenario] with [scenarioId]
	 * in the [ApplicationData] of [application].
	 */
	fun deleteScenarioStep(dataHolder: UndoableDataHolder, scenarioId: Int, scenarioStepId: Int) {
		LOG.trace("Delete ScenarioStep $scenarioStepId from Scenario $scenarioId in GraphView ${graphView(dataHolder).graph?.uuid}")
		commandManager.execute(DeleteScenarioStepCommand(dataHolder, scenarioId, scenarioStepId))
	}

	/**
	 * Moved the specified [ScenarioStep] within its [Scenario], i.e. changes the position in the ordered list.
	 */
	fun moveScenarioStep(dataHolder: UndoableDataHolder, scenarioId: Int, scenarioStepId: Int, newIndex: Int) {
		LOG.trace("Move ScenarioStep $scenarioStepId in Scenario $scenarioId to new index $newIndex in GraphView ${graphView(dataHolder).graph?.uuid}")
		commandManager.execute(MoveScenarioStepCommand(dataHolder, scenarioId, scenarioStepId, newIndex))
	}

	fun moveScenario(dataHolder: UndoableDataHolder, scenarioId: Int, newIndex: Int) {
		LOG.trace("Move Scenario $scenarioId to new index $newIndex in GraphView ${graphView(dataHolder).graph?.uuid}")
		commandManager.execute(MoveScenarioCommand(dataHolder, scenarioId, newIndex))
	}
}