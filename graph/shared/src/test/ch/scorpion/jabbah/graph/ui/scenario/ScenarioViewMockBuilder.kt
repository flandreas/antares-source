package ch.scorpion.jabbah.graph.ui.scenario

import io.mockk.mockk

class ScenarioViewMockBuilder(controller: ScenarioViewController) {

	private val view = mockk<ScenarioView>(relaxed = true)

	init {
		controller.view = view
	}

	fun build(): ScenarioView = view
}