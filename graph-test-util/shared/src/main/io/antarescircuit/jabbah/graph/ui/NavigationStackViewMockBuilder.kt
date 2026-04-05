package io.antarescircuit.jabbah.graph.ui

import dev.mokkery.MockMode
import dev.mokkery.mock

class NavigationStackViewMockBuilder(controller: NavigationStackViewController) {

	private val view = mock<NavigationStackView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): NavigationStackView = view
}