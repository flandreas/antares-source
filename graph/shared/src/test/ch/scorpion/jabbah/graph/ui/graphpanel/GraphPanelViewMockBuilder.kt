package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.execution.issue.IssuesView
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopView
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemMockBuilder
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewMockBuilder
import ch.scorpion.jabbah.graph.ui.hierarchy.GraphHierarchyView
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelView
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelViewMockBuilder
import ch.scorpion.jabbah.graph.ui.logview.LogView
import ch.scorpion.jabbah.graph.ui.logview.LogViewMockBuilder
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.every
import io.mockk.mockk

class GraphPanelViewMockBuilder(private val controller: GraphPanelViewController) {

	private val graphPanelView = mockk<GraphPanelView>(relaxed = true)

	init {
		controller.view = graphPanelView
		withLibraryPanel(LibraryPanelViewMockBuilder(controller.libraryPanelController).build())

		val editView = GraphEditViewMockBuilder(controller.editViewController).build()
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