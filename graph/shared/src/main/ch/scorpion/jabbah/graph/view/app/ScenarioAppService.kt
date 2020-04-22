package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.graph.view.scenario.*

class ScenarioAppService(
	private val commandManager: CommandManager = EditModule.commandManager
) {

	companion object {
		private val LOG by logger(ScenarioAppService::class)
	}

	private fun graphView(application: Application): GraphView = (application.data!!.content as MetaGraph).graph.graphView

	/**
	 * Creates a clone of [scenario] and adds it to the [GraphView] in the [ApplicationData] of [application].
	 * @return the ID of the cloned [Scenario]
	 */
	fun addScenario(application: Application, scenario: Scenario): Int {
		LOG.debug("Add new Scenario to GraphView ${graphView(application).graph?.uuid}")
		val command = AddScenarioCommand(application, scenario)
		commandManager.execute(command)
		return command.addedScenarioId
	}

	/** Deletes the [Scenario] with the specified ID from the [GraphView] in the [ApplicationData] of [application]. */
	fun deleteScenario(application: Application, scenarioId: Int) {
		LOG.debug("Delete Scenario $scenarioId from GraphView ${graphView(application).graph?.uuid}")
		commandManager.execute(DeleteScenarioCommand(application, scenarioId))
	}

	/**
	 * Creates a clone of [scenarioStep] and adds it to the specified [Scenario] in the [ApplicationData]
	 * of [application].
	 */
	fun addScenarioStep(application: Application, scenarioId: Int, scenarioStep: ScenarioStep): Int {
		LOG.debug("Add new ScenarioStep to Scenario $scenarioId in GraphView ${graphView(application).graph?.uuid}")
		val command = AddScenarioStepCommand(application, scenarioId, scenarioStep)
		commandManager.execute(command)
		return command.addedScenarioStepId
	}

	/**
	 * Deleted the [ScenarioStep] with ID [scenarioStepId] from the [Scenario] with [scenarioId]
	 * in the [ApplicationData] of [application].
	 */
	fun deleteScenarioStep(application: Application, scenarioId: Int, scenarioStepId: Int) {
		LOG.debug("Delete ScenarioStep $scenarioStepId from Scenario $scenarioId in GraphView ${graphView(application).graph?.uuid}")
		commandManager.execute(DeleteScenarioStepCommand(application, scenarioId, scenarioStepId))
	}

	/**
	 * Moved the specified [ScenarioStep] within its [Scenario], i.e. changes the position in the ordered list.
	 */
	fun moveScenarioStep(application: Application, scenarioId: Int, scenarioStepId: Int, newIndex: Int) {
		LOG.debug("Move ScenarioStep $scenarioStepId in Scenario $scenarioId to new index $newIndex in GraphView ${graphView(application).graph?.uuid}")
		commandManager.execute(MoveScenarioStepCommand(application, scenarioId, scenarioStepId, newIndex))
	}
}