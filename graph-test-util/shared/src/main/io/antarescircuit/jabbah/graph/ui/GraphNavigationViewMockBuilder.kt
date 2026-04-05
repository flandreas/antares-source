package io.antarescircuit.jabbah.graph.ui

import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.mock

class GraphNavigationViewMockBuilder(private val controller: GraphNavigationViewController) {

	private val view = mock<GraphNavigationView>(MockMode.autofill)

	init {
		controller.view = view
		withNavigationStackView(
			NavigationStackViewMockBuilder(controller.navigationStackViewController)
				.build())

		every { view.drawingView } calls { controller.drawingView }
	}

	fun withNavigationStackView(view: NavigationStackView): GraphNavigationViewMockBuilder {
		controller.navigationStackViewController.view = view
		return this
	}

	fun build(): GraphNavigationView = view
}