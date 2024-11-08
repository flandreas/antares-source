package ch.scorpion.jabbah.graph.ui.usecase

import dev.mokkery.MockMode
import dev.mokkery.mock

class UsecaseViewMockBuilder(controller: UsecaseViewController) {

	private val view = mock<UsecaseView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): UsecaseView = view
}