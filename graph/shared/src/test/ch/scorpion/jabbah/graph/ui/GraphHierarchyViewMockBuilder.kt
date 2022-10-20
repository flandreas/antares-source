package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.hierarchy.GraphHierarchyController
import ch.scorpion.jabbah.graph.ui.hierarchy.GraphHierarchyView
import io.mockk.mockk

class GraphHierarchyViewMockBuilder(controller: GraphHierarchyController) {

	private val view = mockk<GraphHierarchyView>(relaxed = true)

	init {
		controller.view = view
	}

	fun build(): GraphHierarchyView = view
}