package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioPropertyPanel
import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioView
import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioViewController
import dev.mokkery.MockMode
import dev.mokkery.mock

class ScenarioViewMockBuilder(private val controller: ScenarioViewController) {

	private val view = mock<ScenarioView>(MockMode.autofill)

	init {
		controller.view = view
		withScenarioPropertyPanel(ScenarioPropertyPanelMockBuilder(controller.propertyPanelController).build())
	}

	fun withScenarioPropertyPanel(view: ScenarioPropertyPanel): ScenarioViewMockBuilder {
		controller.propertyPanelController.view = view
		return this
	}

	fun build(): ScenarioView = view
}