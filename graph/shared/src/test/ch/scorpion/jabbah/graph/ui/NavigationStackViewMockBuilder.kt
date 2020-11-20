package ch.scorpion.jabbah.graph.ui

import io.mockk.mockk

class NavigationStackViewMockBuilder(controller: NavigationStackViewController) {

	private val view = mockk<NavigationStackView>(relaxed = true)

	init {
		controller.view = view
	}

	fun build(): NavigationStackView = view
}