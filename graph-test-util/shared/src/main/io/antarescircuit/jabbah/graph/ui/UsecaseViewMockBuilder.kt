package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.graph.ui.usecase.UsecaseView
import io.antarescircuit.jabbah.graph.ui.usecase.UsecaseViewController
import dev.mokkery.MockMode
import dev.mokkery.mock

class UsecaseViewMockBuilder(controller: UsecaseViewController) {

	private val view = mock<UsecaseView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): UsecaseView = view
}