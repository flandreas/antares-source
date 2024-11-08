package ch.scorpion.jabbah.graph.ui.scenario

import dev.mokkery.MockMode
import dev.mokkery.mock

class ScenarioPropertyPanelMockBuilder(controller: ScenarioPropertyPanelController) {

	private val view = mock<ScenarioPropertyPanel>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): ScenarioPropertyPanel = view
}