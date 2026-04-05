package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.edit.ComponentPropertyPanelMockBuilder
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanel
import io.antarescircuit.jabbah.execution.IssuesViewMockBuilder
import io.antarescircuit.jabbah.execution.issue.IssuesView
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopView
import io.antarescircuit.jabbah.graph.ui.graphpanel.GraphPanelView
import io.antarescircuit.jabbah.graph.ui.graphpanel.GraphPanelViewController
import io.antarescircuit.jabbah.graph.ui.hierarchy.GraphHierarchyView
import io.antarescircuit.jabbah.graph.ui.library.LibraryPanelView
import io.antarescircuit.jabbah.graph.ui.logview.LogView
import io.antarescircuit.jabbah.graph.view.GraphView
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

class GraphPanelViewMockBuilder(private val controller: GraphPanelViewController) {

	private val graphPanelView = mock<GraphPanelView>(MockMode.autofill)

	val graphEditViewBuilder = GraphEditViewMockBuilder(controller.editViewController)

	val libraryPanelBuilder = LibraryPanelViewMockBuilder(controller.libraryPanelController)

	init {
		controller.view = graphPanelView
		withLibraryPanel(libraryPanelBuilder.build())

		val editView = graphEditViewBuilder.build()
		withGraphEditView(editView)
		withGraphDesktopView(GraphDesktopViewMockBuilder(controller.desktopController)
			.withMainViewItem(editView.graphNavigationView)
			.withMainViewItem(
				GraphDesktopViewItemMockBuilder()
				.withDrawingView(controller.editor.view as DrawingView<GraphView>)
				.build())
			.build()
		)
		withIssuesView(IssuesViewMockBuilder(controller.issuesViewController).build())
		withLogView(LogViewMockBuilder(controller.logViewController).build())
		withComponentPropertiesPanel(ComponentPropertyPanelMockBuilder(controller.propertyPanelController).build())
		withGraphHierarchyView(GraphHierarchyViewMockBuilder(controller.graphHierarchyController).build())
	}

	fun withLibraryPanel(view: LibraryPanelView): GraphPanelViewMockBuilder {
		controller.libraryPanelController.view = view
		return this
	}

	fun withGraphEditView(view: GraphEditView): GraphPanelViewMockBuilder {
		every { graphPanelView.graphEditView } returns view
		controller.editViewController.view = view
		return this
	}

	fun withGraphDesktopView(view: GraphDesktopView): GraphPanelViewMockBuilder {
		controller.desktopController.view = view
		return this
	}

	fun withIssuesView(view: IssuesView): GraphPanelViewMockBuilder {
		controller.issuesViewController.view = view
		return this
	}

	fun withLogView(view: LogView): GraphPanelViewMockBuilder {
		controller.logViewController.view = view
		return this
	}

	fun withComponentPropertiesPanel(view: ComponentPropertyPanel): GraphPanelViewMockBuilder {
		controller.propertyPanelController.view = view
		return this
	}

	fun withGraphHierarchyView(view: GraphHierarchyView): GraphPanelViewMockBuilder {
		controller.graphHierarchyController.view = view
		return this
	}

	fun build(): GraphPanelView = graphPanelView
}