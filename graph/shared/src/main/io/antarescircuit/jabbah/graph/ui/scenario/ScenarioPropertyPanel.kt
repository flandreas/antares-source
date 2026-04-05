package io.antarescircuit.jabbah.graph.ui.scenario

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.properties.AbstractPropertyPanelController
import io.antarescircuit.jabbah.edit.properties.PropertyPanel
import io.antarescircuit.jabbah.graph.view.Scenario
import io.antarescircuit.jabbah.graph.view.ScenarioStep

interface ScenarioPropertyPanel : PropertyPanel

/**
 * Displays the properties of the currently selected [Scenario] or [ScenarioStep]
 * and allows the user to edit them.
 */
class ScenarioPropertyPanelController(
	editor: Editor,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractPropertyPanelController<ScenarioPropertyPanel>(editor) {

	private val scenarioSelectionHandler: EventHandler<ScenarioSelectionEvent> = {
		bean = it.scenarioStep ?: it.scenario
	}

	init {
		eventBus.register(ScenarioSelectionEvent::class, scenarioSelectionHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(scenarioSelectionHandler)
	}

	override val description: String?
		get() = when (bean) {
			is Scenario -> {
				(bean as Scenario).name.value
			}
			is ScenarioStep -> {
				(bean as ScenarioStep).name.value
			}
			else -> {
				null
			}
		}
}