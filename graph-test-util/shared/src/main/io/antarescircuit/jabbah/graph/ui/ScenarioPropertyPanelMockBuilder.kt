package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioPropertyPanel
import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioPropertyPanelController
import dev.mokkery.MockMode
import dev.mokkery.mock

class ScenarioPropertyPanelMockBuilder(controller: ScenarioPropertyPanelController) {

	private val view = mock<ScenarioPropertyPanel>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): ScenarioPropertyPanel = view
}