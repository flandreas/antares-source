package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario

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