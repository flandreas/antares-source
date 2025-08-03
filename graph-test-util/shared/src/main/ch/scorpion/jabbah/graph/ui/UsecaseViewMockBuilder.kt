package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewController
import dev.mokkery.MockMode
import dev.mokkery.mock

class UsecaseViewMockBuilder(controller: UsecaseViewController) {

	private val view = mock<UsecaseView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): UsecaseView = view
}