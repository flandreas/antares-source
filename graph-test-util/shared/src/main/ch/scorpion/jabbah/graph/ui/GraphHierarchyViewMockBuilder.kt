package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.hierarchy.GraphHierarchyController
import ch.scorpion.jabbah.graph.ui.hierarchy.GraphHierarchyView
import dev.mokkery.MockMode
import dev.mokkery.mock

class GraphHierarchyViewMockBuilder(controller: GraphHierarchyController) {

	private val view = mock<GraphHierarchyView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): GraphHierarchyView = view
}