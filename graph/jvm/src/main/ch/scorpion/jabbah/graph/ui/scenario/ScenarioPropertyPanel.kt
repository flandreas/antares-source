package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
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

	private var currentSavable: Savable? = null

    init {
        eventBus.register(ScenarioSelectionEvent::class) {
	        clearProperties()
	        if (it.scenarioStep != null) {
		        loadProperties(it.scenarioStep)
	        } else if (it.scenario != null) {
		        loadProperties(it.scenario)
	        }
        }

	    eventBus.register(CurrentSavableEvent::class) {
		    this.currentSavable = it.savable
		    updateEnabledness()
	    }

	    updateEnabledness()
    }

	override fun setupDefaultProperties() {
		// empty
	}

    override fun getDescription(bean: Any): String? {
        if (bean is Scenario) {
            return bean.name.value
        }
        if (bean is ScenarioStep) {
            return bean.name.value
        }
        return null
    }

	private fun updateEnabledness() {
		getTable().isEnabled = !(currentSavable?.readOnly ?: false)
	}
}