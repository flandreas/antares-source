package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.scenario.AddScenarioCommand
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JOptionPane

/**
 * Asks the user for the name of a new [Scenario] and adds it to the current [Graph].
 */
class AddScenarioAction(
    eventBus: EventBus,
    private val cmdManager: CommandManager
) : AbstractAction("scenarios.action.addScenario") {

    private var graphView: GraphView<*>? = null

    init {
        eventBus.register(ScenarioSelectionEvent::class, {
            graphView = it.graphView
            isEnabled = it.scenario == null && it.scenarioStep == null
        })
        isEnabled = false
    }

    override fun actionPerformed(e: ActionEvent?) {
        val name = JOptionPane.showInputDialog(
            null,
            Translations.getString("scenarios.action.addScenario.question"),
            getValue(Action.NAME) as String,
            JOptionPane.QUESTION_MESSAGE
        )
        if (StringUtils.isEmpty(name)) {
            return
        }
        cmdManager.execute(AddScenarioCommand(graphView!!, ScenarioImpl(name)))
    }
}