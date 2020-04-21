package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import javax.swing.Action
import javax.swing.JMenu
import javax.swing.JMenuItem

/**
 * Utility class for building menus related with [Scenario]s and [ScenarioStep]s.
 */
object ScenarioMenuBuilder {

	/**
	 * Builds a [JMenuItem] for every [Scenario] of the specified [GraphView]
	 * @param actionBuilder builds the [Action] for a particular [Scenario].
	 */
	fun buildScenarioMenu(graphView: GraphView, actionBuilder: (Scenario) -> Action): List<JMenuItem> {
		val list = mutableListOf<JMenuItem>()
		for (scenario in graphView.scenarios.getScenarios()) {
			list.add(JMenuItem(actionBuilder.invoke(scenario)))
		}
		return list
	}

	/**
	 * Builds a [List] of [JMenuItem]s that represent all [ScenarioStep]s (grouped by they
	 * [Scenario]s to which they belong) of a [GraphView].
	 *
	 * @param graphView the [GraphView] for which [ScenarioStep] menus are to be build.
	 * @param actionBuilder builds the [Action] to be associated with a built [JMenuItem] of a [ScenarioStep].
	 * @return a structure with a [JMenuItem] for every [ScenarioStep] of a [GraphView], grouped by a
	 *         [JMenu] for the corresponding [Scenario].
	 */
	fun buildScenarioStepMenu(graphView: GraphView, actionBuilder: (ScenarioStep) -> Action): List<JMenuItem> {
		val list = mutableListOf<JMenuItem>()
		for (scenario in graphView.scenarios.getScenarios()) {
			if (scenario.stepCount > 0) {
				val menu = JMenu(scenario.name.value)
				for (scenarioStep in scenario.getScenarioSteps()) {
					menu.add(JMenuItem(actionBuilder.invoke(scenarioStep)))
				}
			}
		}
		return list
	}
}