package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.edit.properties.AbstractPropertyPanelSwing
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep

/**
 * An [AbstractPropertyPanelSwing] for editing the properties of the currently selected
 * [Scenario] or [ScenarioStep].
 */
class ScenarioPropertyPanelSwing(
	controller: ScenarioPropertyPanelController,
	sheetPanelFactory: PropertySheetPanelFactory
) : AbstractPropertyPanelSwing(controller, sheetPanelFactory), ScenarioPropertyPanel {

	init {
		controller.view = this
	}

	override fun setupDefaultProperties() {
		// empty
	}
}