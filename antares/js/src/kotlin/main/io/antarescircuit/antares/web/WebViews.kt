package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.app.ApplicationDataView
import io.antarescircuit.jabbah.app.ModalMessageType
import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.app.SaveUnchangedDataDecision
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanel
import io.antarescircuit.jabbah.execution.issue.IssuesView
import io.antarescircuit.jabbah.graph.ui.GraphEditView
import io.antarescircuit.jabbah.graph.ui.GraphFrame
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import io.antarescircuit.jabbah.graph.ui.GraphNavigationView
import io.antarescircuit.jabbah.graph.ui.NavigationStackEntry
import io.antarescircuit.jabbah.graph.ui.NavigationStackView
import io.antarescircuit.jabbah.graph.ui.NavigationStackViewController
import io.antarescircuit.jabbah.graph.ui.container.ContainerPanelView
import io.antarescircuit.jabbah.graph.ui.container.SymbolComparatorView
import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioPropertyPanel
import io.antarescircuit.jabbah.graph.ui.usecase.UsecasePropertyPanel
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopView
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.ui.documentation.DocumentationPanelController
import io.antarescircuit.jabbah.graph.ui.documentation.DocumentationPanelView
import io.antarescircuit.jabbah.graph.ui.graphpanel.GraphPanelView
import io.antarescircuit.jabbah.graph.ui.graphpanel.IssuesSummary
import io.antarescircuit.jabbah.graph.ui.hierarchy.GraphHierarchyView
import io.antarescircuit.jabbah.graph.ui.library.LibraryPanelView
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreePanel
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeView
import io.antarescircuit.jabbah.graph.ui.logview.LogView
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryFilter
import io.antarescircuit.jabbah.graph.library.LibraryImportsEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemAddedEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemMovedEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemRemovedEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemUpdatedEvent
import io.antarescircuit.jabbah.graph.library.LibraryRenamedEvent
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.graph.library.LibraryDirectory
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.io.Storable

/*
 * Browser implementations of the views required by the shared UI controllers.
 * The actual UI is rendered by the web application, so most views only notify it about changes.
 */

/** Signals the web application that the editor state changed and should be re-read. */
internal typealias Notify = () -> Unit

internal class WebApplicationDataView(
	private val notify: Notify,
	private val message: (type: String, title: String, text: String) -> Unit,
) : ApplicationDataView {
	override fun dispose() {}

	/** Changes are always kept: the web editor saves automatically. */
	override fun decideSaveChangedData(action: String) = SaveUnchangedDataDecision.Yes

	override fun defineSavableForStoring(storable: Storable, currentSavable: Savable?): Savable? = currentSavable

	override fun defineSavableForLoading(): Savable? = null

	override fun showModalMessage(type: ModalMessageType, title: String, message: String) {
		message(type.name.lowercase(), title, message)
		notify()
	}

	override fun registerKeepAliveUsage() {}
}

/**
 * The single navigation view of the main editor, also acting as its edit view and navigation stack view.
 */
internal class WebGraphEditView(
	private val controllerProvider: () -> NavigationStackViewController,
	private val drawingViewProvider: () -> DrawingView<GraphElementView<*>, GraphView>,
	private val notify: Notify,
) : GraphEditView, GraphNavigationView, NavigationStackView {

	override fun dispose() {}

	/** ---- [GraphEditView] */
	override val graphNavigationView: GraphNavigationView get() = this

	/** ---- [GraphNavigationView] / [GraphDesktopViewItem] */
	override val layoutWidth: Int get() = drawingView.width
	override val layoutHeight: Int get() = drawingView.height
	override val graphView: GraphView get() = drawingView.drawing
	override val reusable: Boolean get() = true
	override val showsNavigationRoot: Boolean get() = controller.navigationStack.size <= 1
	override fun refresh() = notify()
	override val drawingView: DrawingView<GraphElementView<*>, GraphView> get() = drawingViewProvider()
	override var contextColor: CompositeColor? = null
	override val isDetached: Boolean = false
	override fun disposeItem() {}
	override fun findContent(condition: (DrawingViewContent<GraphElementView<*>, GraphView>) -> Boolean): DrawingViewContent<*, *>? = null
	override fun createCloseRequest(): Any = "Close Request"
	override fun displays(content: Any?): Boolean = content === graphView
	override val mainUI: Any? get() = null
	override val view: View<out EditInputEventContext> get() = drawingView

	/** ---- [NavigationStackView] */
	override val controller: NavigationStackViewController get() = controllerProvider()
	override var editable: Boolean = false
		set(value) {
			field = value
			notify()
		}
	override var active: Boolean = false
}

internal class WebGraphPanelView(
	override val graphEditView: GraphEditView,
	private val notify: Notify,
) : GraphPanelView {
	override var issuesSummary: IssuesSummary? = null
		set(value) {
			field = value
			notify()
		}

	override fun updateDynamicToolbar() = notify()
	override fun dispose() {}
}

internal class WebGraphFrame(
	private val controller: () -> GraphFrameController<*>,
	private val onDisplayedViewChanged: () -> Unit,
) : GraphFrame {
	override val applicationMode get() = controller().applicationModeHolder.currentMode
	override val desktopView: View<*> get() = controller().editor.view
	override val containerView: View<*> get() = controller().containerPanelController.drawingView
	override val desktopViewShowsNavigationRoot: Boolean get() = true
	override fun notifyDisplayedView() = onDisplayedViewChanged()
	override fun dispose() {}
}

/**
 * Reports which item is shown in the main area (the circuit editor or a [WebDocumentItem])
 * and which item is opened additionally, e.g. memory contents, to the web editor.
 */
internal class WebGraphDesktopView(
	private val onMainItem: (GraphDesktopViewItem?) -> Unit,
	private val onChildItem: (GraphDesktopViewItem?) -> Unit,
	private val createSideView: (SubGraphVerticeView<*>, CompositeColor?, Boolean) -> GraphDesktopViewItem,
	private val onSideViewsChanged: (shown: GraphDesktopViewItem?, closed: GraphDesktopViewItem?) -> Unit,
) : GraphDesktopView {
	override fun createSubGraphDesktopItem(
		verticeView: SubGraphVerticeView<*>,
		referenceColor: CompositeColor?,
		isParentDetached: Boolean,
		viewManager: ContentViewManager
	): GraphDesktopViewItem = createSideView(verticeView, referenceColor, isParentDetached)

	override fun showMainItem(item: GraphDesktopViewItem) = onMainItem(item)

	override fun showChildItem(item: GraphDesktopViewItem) =
		if (item is WebSideView) onSideViewsChanged(item, null) else onChildItem(item)

	override fun closeChildItem(item: GraphDesktopViewItem) =
		if (item is WebSideView) onSideViewsChanged(null, item) else onChildItem(null)

	override fun closeAll() {
		onSideViewsChanged(null, null)
		onChildItem(null)
		onMainItem(null)
	}
	override fun dispose() {}
}

internal class WebContainerPanelView(private val notify: Notify) : ContainerPanelView {
	override val rightSidebarOpen: Boolean = false
	override fun dataChanged() = notify()
	override fun updateIsManualContainer(isManualContainer: Boolean) = notify()
	override fun confirmGenerateContainerDrawing(title: String): Boolean = true
	override fun generateContainerDrawing() {}
	override fun activeChanged() = notify()
	override fun dispose() {}
}

internal class WebDocumentationPanelView(
	private val controller: DocumentationPanelController,
	private val notify: Notify,
) : DocumentationPanelView {
	/**
	 * The displayed text: follows the model, and is set by the web application before asking the controller
	 * to store edited documentation. The controller writes it into circuits being saved, so it must not lag behind.
	 */
	var text: String = ""
	override val viewText: String get() = text
	override fun notifyModelDataChanged() {
		text = controller.text ?: ""
		notify()
	}
	override fun notifyEditabilityChanged() = notify()
	override fun notifyModeChanged() = notify()
	override fun refreshPreview() {}
	override fun dispose() {}
}

internal class WebLibraryViews(private val notify: Notify) : LibraryPanelView, LibraryTreeView, LibraryTreePanel {
	override fun refresh() = notify()
	override fun dispose() {}

	override fun openMainLibrary(library: Library) = notify()
	override fun closeMainLibrary() = notify()
	override fun reload() = notify()
	override fun expandTo(element: ContainerLibraryElement) {}
	override fun expandFolder(folderName: String) {}
	override fun expandToCurrentSavable() {}
	override fun expandAllFromSelection() {}
	override fun collapseAtSelection() {}

	override fun handle(event: LibraryItemAddedEvent) = notify()
	override fun handle(event: LibraryItemRemovedEvent) = notify()
	override fun handle(event: LibraryItemUpdatedEvent) = notify()
	override fun handle(event: LibraryItemMovedEvent) = notify()
	override fun handle(event: LibraryRenamedEvent) = notify()

	override val folderOfSelectedItem: LibraryDirectory? get() = null

	override fun filter(filter: LibraryFilter?) {}
	override fun clearFilter() {}
}

/** Collects all remaining views that only need to notify the web application. */
internal class WebPassiveViews(private val notify: Notify) :
	ComponentPropertyPanel, ScenarioPropertyPanel, UsecasePropertyPanel, SymbolComparatorView,
	IssuesView, LogView, GraphHierarchyView {
	override fun dispose() {}
	override fun reset() {}
	override fun handleBeanReplaced() = notify()
	override fun refresh() = notify()
	override fun notifyNewIssues() = notify()
	override fun showCannotOpenMessage() {}
	override fun refresh(oldColumnsCount: Int) {}
	override fun handleRemove(subGraphVerticeView: SubGraphVerticeView<*>) {}
}

internal fun rootEntryFor(view: GraphNavigationView) = NavigationStackEntry(content = view.drawingView!!.content)
