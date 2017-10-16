package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.graph.view.scenario.*
import java.awt.Frame
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JOptionPane

/**
 * Asks the user for the name of a new [Scenario] and adds it to the current [Graph].
 */
class AddScenarioAction(
        eventBus: EventBus = BaseModule.eventBus,
        private val cmdManager: CommandManager = EditModule.commandManager
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
                Frame.getFrames()[0],
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

/**
 * Asks the user for the name of a new [ScenarioStep] and adds it to the current [Scenario].
 */
class AddScenarioStepAction(
        eventBus: EventBus = BaseModule.eventBus,
        private val cmdManager: CommandManager = EditModule.commandManager,
        private val scriptGateway: ScriptGateway = ScriptModule.scriptGateway
) : AbstractAction("scenarios.action.addScenarioStep") {

    private var graphView: GraphView<*>? = null
    private var scenario: Scenario? = null

    init {
        eventBus.register(ScenarioSelectionEvent::class, {
            graphView = it.graphView
            scenario = it.scenario
            isEnabled = graphView != null && scenario != null && it.scenarioStep == null
        })
    }

    override fun actionPerformed(e: ActionEvent?) {
        val name = JOptionPane.showInputDialog(
                Frame.getFrames()[0],
                Translations.getString("scenarios.action.addScenarioStep.question"),
                getValue(Action.NAME) as String,
                JOptionPane.QUESTION_MESSAGE
        )
        if (StringUtils.isEmpty(name)) {
            return
        }
        cmdManager.execute(AddScenarioStepCommand(graphView!!, scenario!!, ScenarioStepImpl(scriptGateway, name)))
    }
}

/** Deletes the currently selected [Scenario]. */
class DeleteScenarioAction(
        eventBus: EventBus = BaseModule.eventBus,
        private val cmdManager: CommandManager = EditModule.commandManager
) : AbstractAction("scenarios.action.deleteScenario") {

    private var graphView: GraphView<*>? = null
    private var scenario: Scenario? = null

    init {
        eventBus.register(ScenarioSelectionEvent::class, {
            graphView = it.graphView
            scenario = it.scenario
            isEnabled = graphView != null && scenario != null && it.scenarioStep == null
        })
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (JOptionPane.showConfirmDialog(
                Frame.getFrames()[0],
                Translations.getString("scenarios.action.deleteScenario.question", scenario!!.name),
                Translations.getString("scenarios.action.deleteScenario.name"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
        {
            cmdManager.execute(DeleteScenarioCommand(graphView!!, scenario!!))
        }
    }
}

/** Deletes the currently selected [ScenarioStep]. */
class DeleteScenarioStepAction(
        eventBus: EventBus = BaseModule.eventBus,
        private val cmdManager: CommandManager = EditModule.commandManager
) : AbstractAction("scenarios.action.deleteScenarioStep") {

    private var graphView: GraphView<*>? = null
    private var scenario: Scenario? = null
    private var scenarioStep: ScenarioStep? = null

    init {
        eventBus.register(ScenarioSelectionEvent::class, {
            graphView = it.graphView
            scenario = it.scenario
            scenarioStep = it.scenarioStep
            isEnabled = graphView != null && scenario != null && it.scenarioStep != null
        })
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (JOptionPane.showConfirmDialog(
                Frame.getFrames()[0],
                Translations.getString("scenarios.action.deleteScenarioStep.question", scenarioStep!!.name, scenario!!.name),
                Translations.getString("scenarios.action.deleteScenarioStep.name"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
        {
            cmdManager.execute(DeleteScenarioStepCommand(graphView!!, scenario!!, scenarioStep!!))
        }
    }
}