package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep

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
	val applicationContextHolder: GraphApplicationContextHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<ScenarioView>() {

	val propertyPanelController = ScenarioPropertyPanelController(editor, eventBus)

	/** The [GraphView] whose [Scenario]s and [ScenarioStep]s are displayed. */
	var graphView: GraphView? = null
		set(value) {
			if (field !== value) {
				field = value
				view.graphView = value
			}
		}

	private val scenarioSelectionEventHandler: EventHandler<ScenarioSelectionEvent> = {
		event -> graphView?.let {
			if (event.graphView === graphView) {
				it.currentScenario = event.scenario
				it.currentScenarioStep = event.scenarioStep
			}
		}
	}

	init {
		eventBus.register(ScenarioSelectionEvent::class, scenarioSelectionEventHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(scenarioSelectionEventHandler)
		propertyPanelController.dispose()
	}
}

