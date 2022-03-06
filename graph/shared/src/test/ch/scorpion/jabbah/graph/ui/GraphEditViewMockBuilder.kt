package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewMockBuilder
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewMockBuilder
import io.mockk.every
import io.mockk.mockk

class GraphEditViewMockBuilder(private val controller: GraphEditViewController) {

	private val graphEditView = mockk<GraphEditView>(relaxed = true)

	init {
		controller.view = graphEditView
		withGraphNavigationView(GraphNavigationViewMockBuilder(controller.graphNavigationViewController).build())
		withScenarioView(ScenarioViewMockBuilder(controller.scenarioViewController).build())
		withUsecaseView(UsecaseViewMockBuilder(controller.usecaseViewController).build())
	}

	private fun withGraphNavigationView(view: GraphNavigationView): GraphEditViewMockBuilder {
		every { graphEditView.graphNavigationView } returns view
		controller.graphNavigationViewController.view = view
		return this
	}

	private fun withScenarioView(view: ScenarioView): GraphEditViewMockBuilder {
		controller.scenarioViewController.view = view
		return this
	}

	private fun withUsecaseView(view: UsecaseView): GraphEditViewMockBuilder {
		controller.usecaseViewController.view = view
		return this
	}

	fun build(): GraphEditView = graphEditView
}