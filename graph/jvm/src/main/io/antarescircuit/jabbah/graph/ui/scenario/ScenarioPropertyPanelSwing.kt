package io.antarescircuit.jabbah.graph.ui.scenario

import io.antarescircuit.jabbah.edit.properties.AbstractPropertyPanelSwing
import io.antarescircuit.jabbah.edit.properties.PropertySheetPanelFactory
import io.antarescircuit.jabbah.graph.view.Scenario
import io.antarescircuit.jabbah.graph.view.ScenarioStep

/**
 * An [AbstractPropertyPanelSwing] for editing the properties of the currently selected
 * [Scenario] or [ScenarioStep].
 */
class ScenarioPropertyPanelSwing(
	controller: ScenarioPropertyPanelController,
	sheetPanelFactory: PropertySheetPanelFactory
) : AbstractPropertyPanelSwing(controller, "scenario", sheetPanelFactory), ScenarioPropertyPanel {

	init {
		controller.view = this
	}
}