package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPanel
import ch.scorpion.jabbah.execution.issue.IssuesView
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemMockBuilder
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewMockBuilder
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelView
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelViewMockBuilder
import ch.scorpion.jabbah.graph.ui.logview.LogView
import ch.scorpion.jabbah.graph.ui.logview.LogViewMockBuilder
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk

class GraphPanelViewMockBuilder(private val controller: GraphPanelViewController) {

	private val view = mockk<GraphPanelView>(relaxed = true)

	init {
		controller.view = view
		withLibraryPanel(LibraryPanelViewMockBuilder(controller.libraryPanelController).build())
		withGraphEditView(GraphEditViewMockBuilder(controller.editViewController).build())
		withGraphDesktopView(GraphDesktopViewMockBuilder(controller.desktopController)
			.withMainViewItem(GraphDesktopViewItemMockBuilder()
				.withDrawingView(controller.editor.view as DrawingView<GraphView>)
				.build())
			.build()
		)
		withIssuesView(IssuesViewMockBuilder(controller.issuesViewController).build())
		withLogView(LogViewMockBuilder(controller.logViewController).build())
		withComponentPropertiesPanel(ComponentPropertyPanelMockBuilder(controller.propertyPanelController).build())
	}

	fun withLibraryPanel(view: LibraryPanelView): GraphPanelViewMockBuilder {
		controller.libraryPanelController.view = view
		return this
	}

	fun withGraphEditView(view: GraphEditView): GraphPanelViewMockBuilder {
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

	fun build(): GraphPanelView = view
}