package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep

/**
 * Adds a [Scenario] to a [Graph].
 */
class AddScenarioCommand(
        private val graphView: GraphView<*>,
        private val scenario: Scenario
) : AbstractCommand("scenario.command.scenario.add", null) {

    override fun execute() {
        graphView.scenarios.add(scenario)
    }

    override fun undo() {
        graphView.scenarios.remove(scenario)
    }
}

/** Deletes a [Scenario] from a [Graph].*/
class DeleteScenarioCommand(
    private val graphView: GraphView<*>,
    private val scenario: Scenario
) : AbstractCommand("scenario.command.scenario.delete", null) {

    private val index = graphView.scenarios.indexOfScenario(scenario)

    override fun execute() {
        graphView.scenarios.remove(scenario)
    }

    override fun undo() {
        graphView.scenarios.add(scenario, index)
    }
}

/** Adds a [ScenarioStep] to a [Scenario].*/
class AddScenarioStepCommand(
        private val graphView: GraphView<*>,
        private val scenario: Scenario,
        private val scenarioStep: ScenarioStep
) : AbstractCommand("scenario.command.scenarioStep.add") {

    override fun execute() {
        graphView.scenarios.addStep(scenario, scenarioStep)
    }

    override fun undo() {
        graphView.scenarios.removeStep(scenario, scenarioStep)
    }
}

/** Deletes a [ScenarioStep] from its [Scenario].*/
class DeleteScenarioStepCommand(
        private val graphView: GraphView<*>,
        private val scenario: Scenario,
        private val scenarioStep: ScenarioStep
) : AbstractCommand("scenario.command.scenarioStep.delete") {

    private val index = graphView.scenarios.indexOfStep(scenario, scenarioStep)

    override fun execute() {
        graphView.scenarios.removeStep(scenario, scenarioStep)
    }

    override fun undo() {
        graphView.scenarios.addStep(scenario, scenarioStep, index)
    }
}

/** Moves a [ScenarioStep] within its [Scenario], i.e. changes the position in the ordered list.*/
class MoveScenarioStepCommand(
        private val graphView: GraphView<*>,
        private val scenario: Scenario,
        private val scenarioStep: ScenarioStep,
        private val newIndex: Int
) : AbstractCommand("scenario.command.scenarioStep.move") {

    private val oldIndex: Int = graphView.scenarios.indexOfStep(scenario, scenarioStep)

    override fun execute() {
        graphView.scenarios.moveStep(scenario, scenarioStep, newIndex)
    }

    override fun undo() {
        graphView.scenarios.moveStep(scenario, scenarioStep, oldIndex)
    }
}
