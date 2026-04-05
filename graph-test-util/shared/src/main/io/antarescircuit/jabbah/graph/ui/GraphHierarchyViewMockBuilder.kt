package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.graph.ui.hierarchy.GraphHierarchyController
import io.antarescircuit.jabbah.graph.ui.hierarchy.GraphHierarchyView
import dev.mokkery.MockMode
import dev.mokkery.mock

class GraphHierarchyViewMockBuilder(controller: GraphHierarchyController) {

	private val view = mock<GraphHierarchyView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): GraphHierarchyView = view
}