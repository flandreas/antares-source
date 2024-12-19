package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.ui.AbstractApplicationDataEditModeAction
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.graph.view.app.ScenarioAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import ch.scorpion.jabbah.graph.view.scenario.ScenarioStepImpl
import java.awt.Frame
import javax.swing.JOptionPane


abstract class AbstractScenarioAction(
	baseName: String,
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	protected val service: ScenarioAppService = GraphViewModule.scenarioAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractApplicationDataEditModeAction(baseName, applicationDataHolder, applicationModeHolder, eventBus) {

	protected var scenario: Scenario? = null
	protected var scenarioStep: ScenarioStep? = null

	private val scenarioSelectionHandler: EventHandler<ScenarioSelectionEvent> = {
		scenario = it.scenario
		scenarioStep = it.scenarioStep
		updateEnabled()
	}

	init {
		eventBus.register(ScenarioSelectionEvent::class, scenarioSelectionHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(scenarioSelectionHandler)
	}
}

/**
 * Asks the user for the name of a new [Scenario] and adds it to the current [GraphView].
 */
class AddScenarioAction(
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	service: ScenarioAppService = GraphViewModule.scenarioAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractScenarioAction("scenarios.action.addScenario", applicationDataHolder, applicationModeHolder, service, eventBus) {

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

		service.addScenario(applicationDataHolder, ScenarioImpl(name))
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && scenario == null && scenarioStep == null
}

/**
 * Asks the user for the name of a new [ScenarioStep] and adds it to the current [Scenario].
 */
class AddScenarioStepAction(
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	service: ScenarioAppService = GraphViewModule.scenarioAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractScenarioAction("scenarios.action.addScenarioStep", applicationDataHolder, applicationModeHolder, service, eventBus) {

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
		service.addScenarioStep(applicationDataHolder, scenario!!.id, ScenarioStepImpl(initialName = name))
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && scenario != null && scenarioStep == null
}

/** Deletes the currently selected [Scenario]. */
class DeleteScenarioAction(
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	service: ScenarioAppService = GraphViewModule.scenarioAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractScenarioAction("scenarios.action.deleteScenario", applicationDataHolder, applicationModeHolder, service, eventBus) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		if (JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("scenarios.action.deleteScenario.question", scenario!!.name.value),
				Translations.getString("scenarios.action.deleteScenario.name"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
		{
			service.deleteScenario(applicationDataHolder, scenario!!.id)
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && scenario != null && scenarioStep == null
}

/** Deletes the currently selected [ScenarioStep]. */
class DeleteScenarioStepAction(
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	service: ScenarioAppService = GraphViewModule.scenarioAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractScenarioAction("scenarios.action.deleteScenarioStep", applicationDataHolder, applicationModeHolder, service, eventBus) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		if (JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("scenarios.action.deleteScenarioStep.question", scenarioStep!!.name, scenario!!.name),
				Translations.getString("scenarios.action.deleteScenarioStep.name"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
		{
			service.deleteScenarioStep(applicationDataHolder, scenario!!.id, scenarioStep!!.id)
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && scenario != null && scenarioStep != null
}