package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemElementRef
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

class GraphEditViewMockBuilder(private val controller: GraphEditViewController) {

	private val graphEditView = mock<GraphEditView>(MockMode.autofill)

	init {
		controller.view = graphEditView
		withGraphNavigationView(GraphNavigationViewMockBuilder(controller.graphNavigationViewController).build())
		withScenarioView(ScenarioViewMockBuilder(controller.scenarioViewController).build())
		withUsecaseView(UsecaseViewMockBuilder(controller.usecaseViewController).build())
	}

	fun withCreatedElementRef(ref: GraphDesktopViewItemElementRef): GraphEditViewMockBuilder {
		every { graphEditView.createElementRef(any()) } returns ref
		return this
	}

	private fun withGraphNavigationView(view: GraphNavigationView): GraphEditViewMockBuilder {
		every { graphEditView.graphNavigationView } returns view
		every { graphEditView.drawingView } returns view.drawingView
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