package io.antarescircuit.jabbah.graph.ui.scenario

import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Scenario
import io.antarescircuit.jabbah.graph.view.ScenarioStep
import io.antarescircuit.jabbah.graph.view.app.ScenarioAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioImpl
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioStepImpl

/**
 * Posted by [ScenarioView] on its [EventBus] when the user defines the current [Scenario] and/or [ScenarioStep]
 * by changing the selection in the [ScenarioView].
 */
data class ScenarioSelectionEvent(
	val graphView: GraphView,
	val scenario: Scenario?,
	val scenarioStep: ScenarioStep?)

/**
 * Displays the [Scenario]s and [ScenarioStep]s of a [GraphView]
 * and allows the user to inspect, add, remove and edit them.
 *
 * Typical implementations might display these objects as a tree.
 * Posts a [ScenarioSelectionEvent] on its [EventBus] whenever the users changes
 * the selected [Scenario] or [ScenarioStep].
 */
interface ScenarioView : UIView {

	/** The [GraphView] whose [Scenario]s and [ScenarioStep] are displayed. */
	var graphView: GraphView?

	/**
	 * Asks the user for the name of a new [Scenario].
	 * @return `null` if the user cancelled the action
	 */
	fun getNewScenarioName(): String?

	/**
	 * Asks the user for the name of a new [ScenarioStep].
	 * @return `null` if the user cancelled the action
	 */
	fun getNewScenarioStepName(): String?

	/** Asks the user to confirm deleting the current [Scenario]. */
	fun confirmDeleteScenario(): Boolean

	/** Asks the user to confirm deleting the current [ScenarioStep]. */
	fun confirmDeleteScenarioStep(): Boolean
}

/**
 * Controls a [ScenarioView] that displays [Scenario]s and [ScenarioStep]s of a [GraphView].
 *
 * Listens for [ScenarioSelectionEvent]s and updates the current
 * [Scenario] or [ScenarioStep] of the owning [GraphView], which is used
 * for displaying scenario information while editing or simulating.
 */
class ScenarioViewController(
	editor: Editor,
	val applicationDataHolder: ApplicationDataHolder,
	val applicationContextHolder: GraphApplicationContextHolder,
	val applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val service: ScenarioAppService = GraphViewModule.scenarioAppService,
) : AbstractUIController<ScenarioView>() {

	val propertyPanelController = ScenarioPropertyPanelController(editor, eventBus)

	/** The [GraphView] whose [Scenario]s and [ScenarioStep]s are displayed. */
	var graphView: GraphView? = null
		set(value) {
			if (field !== value) {
				field = value
				view.graphView = value
				scenario = null
				scenarioStep = null
				updateActions()
			}
		}

	/** Holds the currently selected [Scenario], if any.*/
	var scenario: Scenario? = null
		private set

	/** Holds the currently selected [ScenarioStep], if any. Only set if [scenario] is also set.*/
	var scenarioStep: ScenarioStep? = null
		private set

	val metaAddAction: Action = MetaAddAction()

	val addScenarioAction = AddScenarioAction(this, eventBus = eventBus)
	val addScenarioStepAction = AddScenarioStepAction(this, eventBus = eventBus)

	val deleteScenarioAction = DeleteScenarioAction(this, eventBus = eventBus)
	val deleteScenarioStepAction = DeleteScenarioStepAction(this, eventBus = eventBus)

	private val scenarioSelectionEventHandler: EventHandler<ScenarioSelectionEvent> = { handle(it) }

	init {
		eventBus.register(ScenarioSelectionEvent::class, scenarioSelectionEventHandler)
		updateActions()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(scenarioSelectionEventHandler)

		addScenarioAction.dispose()
		addScenarioStepAction.dispose()
		deleteScenarioAction.dispose()
		deleteScenarioStepAction.dispose()

		propertyPanelController.dispose()
	}

	fun addScenario(name: String) {
		service.addScenario(applicationDataHolder, ScenarioImpl(name))
	}

	fun addScenarioStep(name: String) {
		service.addScenarioStep(applicationDataHolder, scenario!!.id, ScenarioStepImpl(initialName = name))
	}

	fun deleteScenario() {
		service.deleteScenario(applicationDataHolder, scenario!!.id)
	}

	fun deleteScenarioStep() {
		service.deleteScenarioStep(applicationDataHolder, scenario!!.id, scenarioStep!!.id)
	}

	private fun handle(event: ScenarioSelectionEvent) {
		scenario = event.scenario
		scenarioStep = event.scenarioStep

		graphView?.let {
			if (event.graphView === graphView) {
				it.currentScenario = event.scenario
				it.currentScenarioStep = event.scenarioStep
			}
		}

		updateActions()
	}

	private fun updateActions() {
		addScenarioAction.updateEnabled()
		addScenarioStepAction.updateEnabled()
		deleteScenarioAction.updateEnabled()
		deleteScenarioStepAction.updateEnabled()
		updateMetaAddAction()
	}

	private fun updateMetaAddAction() {
		metaAddAction.enabled = addScenarioAction.enabled || addScenarioStepAction.enabled
		// Use menu item name as tooltip in button
		if (addScenarioStepAction.enabled) {
			metaAddAction.description = addScenarioStepAction.name
		} else if (addScenarioAction.enabled) {
			metaAddAction.description = addScenarioAction.name
		} else {
			metaAddAction.description = null
		}
	}

	private val dummyActionEvent = ActionEvent(null, this, 0, "", 0)

	private inner class MetaAddAction : AbstractAction("scenarios.action.addScenario", "/img/plus-18.png") {
		override fun execute(event: ActionEvent) {
			if (scenario != null) {
				addScenarioStepAction.execute(dummyActionEvent)
			} else {
				addScenarioAction.execute(dummyActionEvent)
			}
		}
	}
}

