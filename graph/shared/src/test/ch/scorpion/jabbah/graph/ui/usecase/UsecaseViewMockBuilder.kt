package ch.scorpion.jabbah.graph.ui.usecase

import io.mockk.mockk

class UsecaseViewMockBuilder(controller: UsecaseViewController) {

	private val view = mockk<UsecaseView>(relaxed = true)

	init {
		controller.view = view
	}

	fun build(): UsecaseView = view
}