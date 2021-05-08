package ch.scorpion.jabbah.graph.ui.scenario

import io.mockk.mockk

class ScenarioPropertyPanelMockBuilder(controller: ScenarioPropertyPanelController) {

	private val view = mockk<ScenarioPropertyPanel>(relaxed = true)

	init {
		controller.view = view
	}

	fun build(): ScenarioPropertyPanel = view
}