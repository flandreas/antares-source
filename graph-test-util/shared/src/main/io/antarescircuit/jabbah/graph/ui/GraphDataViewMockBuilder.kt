package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.ApplicationDataView
import dev.mokkery.MockMode
import dev.mokkery.mock

class GraphDataViewMockBuilder(controller: GraphDataViewController) {

	private val view = mock<ApplicationDataView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): ApplicationDataView = view
}