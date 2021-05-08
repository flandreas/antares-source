package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.ui.AbstractPropertyPanelController
import ch.scorpion.jabbah.edit.ui.PropertyPanel
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep

interface ScenarioPropertyPanel : PropertyPanel

/**
 * Displays the properties of the currently selected [Scenario] or [ScenarioStep]
 * and allows the user to edit them.
 */
class ScenarioPropertyPanelController(
	editor: Editor,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractPropertyPanelController<ScenarioPropertyPanel>(editor) {

	private val scenarioSelectionHandler: EventHandler<ScenarioSelectionEvent> = { handle(it) }

	init {
		eventBus.register(ScenarioSelectionEvent::class, scenarioSelectionHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(scenarioSelectionHandler)
	}

	private fun handle(event: ScenarioSelectionEvent) {
		view.clearProperties()
		if (event.scenarioStep != null) {
			view.loadProperties(event.scenarioStep)
		} else if (event.scenario != null) {
			view.loadProperties(event.scenario)
		}
	}
}