package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewMockBuilder
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewMockBuilder
import io.mockk.mockk

class GraphEditViewMockBuilder(private val controller: GraphEditViewController) {

	private val view = mockk<GraphEditView>(relaxed = true)

	init {
		controller.view = view
		withGraphNavigationView(GraphNavigationViewMockBuilder(controller.graphNavigationViewController).build())
		withScenarioView(ScenarioViewMockBuilder(controller.scenarioViewController).build())
		withUsecaseView(UsecaseViewMockBuilder(controller.usecaseViewController).build())
	}

	fun withGraphNavigationView(view: GraphNavigationView): GraphEditViewMockBuilder {
		controller.graphNavigationViewController.view = view
		return this
	}

	fun withScenarioView(view: ScenarioView): GraphEditViewMockBuilder {
		controller.scenarioViewController.view = view
		return this
	}

	fun withUsecaseView(view: UsecaseView): GraphEditViewMockBuilder {
		controller.usecaseViewController.view = view
		return this
	}

	fun build(): GraphEditView = view
}