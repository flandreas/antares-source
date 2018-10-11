package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.AbstractPropertyPanel
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep

/**
 * A [AbstractPropertyPanel] for editing the properties of the currently selected
 * [Scenario] or [ScenarioStep].
 */
class ScenarioPropertyPanel(
        editor: Editor,
        sheetPanelFactory: PropertySheetPanelFactory,
        eventBus: EventBus
) : AbstractPropertyPanel(editor, sheetPanelFactory) {

    init {
        eventBus.register(ScenarioSelectionEvent::class) {
	        clearProperties()
	        if (it.scenarioStep != null) {
		        updateProperties(it.scenarioStep)
	        } else if (it.scenario != null) {
		        updateProperties(it.scenario)
	        }
        }
    }

	override fun setupDefaultProperties() {
		// empty
	}

    override fun getDescription(bean: Any): String? {
        if (bean is Scenario) {
            return bean.name
        }
        if (bean is ScenarioStep) {
            return bean.name
        }
        return null
    }
}