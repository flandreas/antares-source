package ch.scorpion.jabbah.graph.ui

import io.mockk.every
import io.mockk.mockk

class GraphNavigationViewMockBuilder(private val controller: GraphNavigationViewController) {

	private val view = mockk<GraphNavigationView>(relaxed = true)

	init {
		controller.view = view
		withNavigationStackView(
			NavigationStackViewMockBuilder(controller.navigationStackViewController)
				.build())

		every { view.drawingView } answers { controller.drawingView }
	}

	fun withNavigationStackView(view: NavigationStackView): GraphNavigationViewMockBuilder {
		controller.navigationStackViewController.view = view
		return this
	}

	fun build(): GraphNavigationView = view
}