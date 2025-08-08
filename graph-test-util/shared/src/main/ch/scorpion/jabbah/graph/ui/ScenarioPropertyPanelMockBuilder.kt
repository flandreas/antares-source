package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.scenario.ScenarioPropertyPanel
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioPropertyPanelController
import dev.mokkery.MockMode
import dev.mokkery.mock

class ScenarioPropertyPanelMockBuilder(controller: ScenarioPropertyPanelController) {

	private val view = mock<ScenarioPropertyPanel>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): ScenarioPropertyPanel = view
}