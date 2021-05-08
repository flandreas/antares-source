package ch.scorpion.jabbah.graph.ui.scenario

import io.mockk.mockk

class ScenarioViewMockBuilder(private val controller: ScenarioViewController) {

	private val view = mockk<ScenarioView>(relaxed = true)

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