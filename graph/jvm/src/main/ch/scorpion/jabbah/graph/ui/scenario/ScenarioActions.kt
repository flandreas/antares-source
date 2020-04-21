package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
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
import ch.scorpion.jabbah.graph.ui.EditedGraphViewEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.graph.view.scenario.*
import java.awt.Frame
import javax.swing.JOptionPane


abstract class AbstractScenarioAction(
	baseName: String,
	protected val cmdManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(baseName) {

	private var currentSavable: Savable? = null
	protected var editedGraphView: GraphView? = null
	protected var graphView: GraphView? = null
	protected var scenario: Scenario? = null
	protected var scenarioStep: ScenarioStep? = null

	init {
		eventBus.register(EditedGraphViewEvent::class) {
			editedGraphView = it.newGraphView
			updateEnabledness()
		}
		eventBus.register(ScenarioSelectionEvent::class) {
			graphView = it.graphView
			scenario = it.scenario
			scenarioStep = it.scenarioStep
			updateEnabledness()
		}
		eventBus.register(CurrentSavableEvent::class) {
			currentSavable = it.savable
			updateEnabledness()
		}
		enabled = false
	}

	protected fun updateEnabledness() {
		enabled = calculateEnabled()
	}

	protected open fun calculateEnabled(): Boolean {
		return editedGraphView != null && !(currentSavable?.readOnly ?: false)
	}
}

/**
 * Asks the user for the name of a new [Scenario] and adds it to the current [GraphView].
 */
class AddScenarioAction : AbstractScenarioAction("scenarios.action.addScenario") {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val name = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("scenarios.action.addScenario.question"),
			name,
			JOptionPane.QUESTION_MESSAGE
		)
		if (StringUtils.isEmpty(name)) {
			return
		}
		cmdManager.execute(AddScenarioCommand(graphView!!, ScenarioImpl(name)))
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && scenario == null && scenarioStep == null
	}
}

/**
 * Asks the user for the name of a new [ScenarioStep] and adds it to the current [Scenario].
 */
class AddScenarioStepAction(
	private val scriptGateway: ScriptGateway = ScriptModule.scriptGateway
) : AbstractScenarioAction("scenarios.action.addScenarioStep") {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val name = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("scenarios.action.addScenarioStep.question"),
			name,
			JOptionPane.QUESTION_MESSAGE
		)
		if (StringUtils.isEmpty(name)) {
			return
		}
		cmdManager.execute(AddScenarioStepCommand(graphView!!, scenario!!, ScenarioStepImpl(scriptGateway, name)))
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && scenario != null && scenarioStep == null
	}
}

/** Deletes the currently selected [Scenario]. */
class DeleteScenarioAction : AbstractScenarioAction("scenarios.action.deleteScenario") {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		if (JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("scenarios.action.deleteScenario.question", scenario!!.name.value),
				Translations.getString("scenarios.action.deleteScenario.name"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
			cmdManager.execute(DeleteScenarioCommand(graphView!!, scenario!!))
		}
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && scenario != null && scenarioStep == null
	}
}

/** Deletes the currently selected [ScenarioStep]. */
class DeleteScenarioStepAction : AbstractScenarioAction("scenarios.action.deleteScenarioStep") {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		if (JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("scenarios.action.deleteScenarioStep.question", scenarioStep!!.name, scenario!!.name),
				Translations.getString("scenarios.action.deleteScenarioStep.name"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
			cmdManager.execute(DeleteScenarioStepCommand(graphView!!, scenario!!, scenarioStep!!))
		}
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && scenario != null && scenarioStep != null
	}
}