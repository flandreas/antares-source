package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataView
import io.mockk.mockk

class GraphDataViewMockBuilder(private val controller: GraphDataViewController) {

	private val view = mockk<ApplicationDataView>(relaxed = true)

	init {
		controller.view = view
	}

	fun build(): ApplicationDataView = view
}