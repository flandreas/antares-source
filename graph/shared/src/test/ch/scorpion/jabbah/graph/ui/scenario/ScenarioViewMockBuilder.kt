package ch.scorpion.jabbah.graph.ui.scenario

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