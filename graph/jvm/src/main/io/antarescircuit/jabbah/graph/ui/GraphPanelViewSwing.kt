package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ToolBar
import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.*
import io.antarescircuit.jabbah.base.ui.TitleBar
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.module.EditModuleJvm
import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanelSwing
import io.antarescircuit.jabbah.edit.properties.PropertySheetPanelFactory
import io.antarescircuit.jabbah.execution.IssuesViewSwing
import io.antarescircuit.jabbah.execution.ResetExecutionTimeAction
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.library.LibraryPanelSwing
import io.antarescircuit.jabbah.graph.ui.desktop.DockingGraphDesktopViewSwing
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopView
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewSwing
import io.antarescircuit.jabbah.graph.ui.graphpanel.GraphPanelView
import io.antarescircuit.jabbah.graph.ui.graphpanel.GraphPanelViewController
import io.antarescircuit.jabbah.graph.ui.graphpanel.IssuesSummary
import io.antarescircuit.jabbah.graph.ui.hierarchy.GraphHierarchyViewSwing
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*


/**
 * A [javax.swing] implementation of [GraphPanelView]
 */
class GraphPanelViewSwing(
	private val controller: GraphPanelViewController,
	private val graphViewAppService: GraphViewAppService = GraphViewModule.graphViewAppService,
	private val eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager,
	application: Application,
	propertySheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel(), GraphPanelView {

	companion object {
		private const val DEF_SIDEBAR_SIZE = 200

		/** The name in [Settings] (extending `propertyBaseName`) of the bottom [JSplitPane] divider position.*/
		private const val BOTTOM_SPLIT_POS = "graphPanel.bottomSidebarSplitPos"
	}

	/** Allows editing and execute the currently open GraphView.*/
	override val graphEditView: GraphEditViewSwing = GraphEditViewSwing(controller.editViewController, viewManager, propertySheetFactory, eventBus)

	/** Allows opening multiple Graphs.*/
	private val desktop: GraphDesktopView = if (BaseModule.properties.getBoolean(GraphDesktopView.PROP_DOCKING)) {
		DockingGraphDesktopViewSwing(controller.desktopController)
	} else {
		GraphDesktopViewSwing(controller.desktopController)
	}

	/** Displays the properties of the currently selected component in [graphEditView].*/
	private val propertyPanel = ComponentPropertyPanelSwing(controller.propertyPanelController, "graph", propertySheetFactory)

	/** Contains UI for selecting components from the current library or the current project.*/
	val libraryPanel = LibraryPanelSwing(controller.libraryPanelController, application, eventBus)

	private val defaultDrawingToolBar = createDrawingToolBar(application, controller)

	private val drawingToolBarHolder = ToolBar().also {
		// Use ToolBar for layout reasons. A Panel with BorderLayout would consume too much space to the right
		it.add(defaultDrawingToolBar, BorderLayout.NORTH)
	}

	private val settingsToolBar = createSettingsToolBar(controller)

	/** Contains the errors view.*/
	private val bottomSidebarPane = SidebarPane(SidebarPane.Location.Bottom) { bottomSidebarPaneChanged() }

	private val executionToolbar: ExecutionToolbarSwing = createExecutionToolBar(controller)

	val toolbars: List<JComponent> = listOf(
		executionToolbar,
		drawingToolBarHolder,
		settingsToolBar
	).onEach { it.isFloatable = false }

	/** The "Explorer" contains [libraryPanel] and [propertyPanel].*/
	private val explorerSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	private val graphHierarchyPanel = GraphHierarchyViewSwing(controller.graphHierarchyController)

	/**
	 * Allows showing and hiding the Explorer. If not initially opened, the event system turns crazy and lags while processing
	 * drag events in the DrawingView (BUG still not explained).
	 */
	private val leftSidebarPane = SidebarSplitPane(
		location = SidebarPane.Location.Left,
		mainContent = desktop as JComponent,
		settingBaseName = "graphPanel.leftSidebar",
		providedInitialOpenIndex = 0,
		contents = listOf(
			SidebarPaneContentImpl(
				Translations.getString("graph.explorer.name"),
				Translations.getString("graph.explorer.desc"),
				UiUtil.themedIcon("/img/compass-16.png"),
				explorerSplitPane,
				listOf(controller.libraryPanelController.libraryTreePanelController.locateMetaGraphAction)),
			SidebarPaneContentImpl(
				Translations.getString("graph.hierarchy.name"),
				Translations.getString("graph.hierarchy.desc"),
				UiUtil.themedIcon("/img/category.png"),
				graphHierarchyPanel)
		))

	private val bottomSidebarSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	/** Holds the location of [bottomSidebarSplitPane]'s divider for re-establishing it the next time it opens.*/
	private var bottomSidebarDividerLocation: Int = BaseModule.settings.getInt(BOTTOM_SPLIT_POS, -1)

	/** Displays the current [Issue]s. */
	private val issuesPanel = IssuesViewSwing(controller.issuesViewController)

	private val issuesContent = SidebarPaneContentImpl(
		Translations.getString("graph.issues.title"),
		Translations.getString("graph.issues.desc"),
		UiUtil.themedIcon("/img/issue-16.png"),
		issuesPanel,
		listOf(controller.issuesViewController.openAction, controller.issuesViewController.clearAction))

	private val logPanel = LogViewSwing(controller.logViewController)

	private val logContent = SidebarPaneContentImpl(
		Translations.getString("graph.log.title"),
		Translations.getString("graph.log.desc"),
		UiUtil.themedIcon("/img/log-16.png"),
		logPanel,
		listOf(controller.logViewController.clearAction))

	val showsNavigationRoot: Boolean get() = graphEditView.graphNavigationView.showsNavigationRoot

	private val titleBar = TitleBar("")

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = { updateTitle() }

	val resetExecutionTimeAction = ResetExecutionTimeAction(controller.applicationContextHolder.scheduler, eventBus)

	init {
		controller.view = this

		(controller.editor.view.canvas as JComponent).transferHandler = createTransferHandler(controller.editor, eventBus)

		buildUI()
		controller.editViewController.setGraphView(controller.editor.drawing as GraphView, true)

		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		updateTitle()

		updateDynamicToolbar()
	}

	override fun dispose() {
		eventBus.unregister(applicationModeHandler)
		BaseModule.settings.set(BOTTOM_SPLIT_POS, bottomSidebarSplitPane.dividerLocation)
		leftSidebarPane.dispose()
		issuesPanel.dispose()
		logPanel.dispose()
		executionToolbar.dispose()
		graphEditView.dispose()
		bottomSidebarPane.dispose()

		BaseModule.settings.set("graphPanel.librarySplitPos", explorerSplitPane.dividerLocation)
	}

	override fun updateDynamicToolbar() {
		drawingToolBarHolder.removeAll()
		if (controller.desktopController.mainDesktopViewItem?.toolBar is JComponent) {
			drawingToolBarHolder.add(controller.desktopController.mainDesktopViewItem!!.toolBar as JComponent)
		} else {
			drawingToolBarHolder.add(defaultDrawingToolBar)
		}
		drawingToolBarHolder.invalidate()
		drawingToolBarHolder.validate()
	}

	private fun updateTitle() {
		when (controller.applicationModeHolder.currentMode) {
			ApplicationMode.EDIT -> titleBar.text = Translations.getString("graph.desktop.title")
			ApplicationMode.EXECUTE, ApplicationMode.EXEC_USECASE -> titleBar.text = Translations.getString("graph.simulator.title")
		}
	}

	override var issuesSummary: IssuesSummary? = null
		set(value) {
			field = value
			val iconPath = when (value?.maxIssueSeverity) {
				null -> "/img/issue-16.png"
				IssueSeverity.Warning -> "/img/warning-16.png"
				IssueSeverity.Error -> "/img/error-16.png"
			}
			val baseTitle = Translations.getString("graph.issues.title")
			val title = when (value?.issuesCount) {
				null, 0 -> baseTitle
				else -> "$baseTitle (${value.issuesCount})"
			}

			issuesContent.name = title
			issuesContent.icon = UiUtil.themedIcon(iconPath)
		}

	fun addBottom(content: SidebarPaneContent) {
		bottomSidebarPane.add(content)
	}

	private fun createTransferHandler(editor: Editor, eventBus: EventBus): TransferHandler =
		GraphPanelTransferHandler(graphViewAppService, editor, eventBus, GraphElementViewTransferable.FLAVOR)

	private fun buildUI() {
		layout = BorderLayout()

		explorerSplitPane.border = null
		libraryPanel.minimumSize = Dimension(libraryPanel.minimumSize.width, 300)
		libraryPanel.preferredSize = Dimension(libraryPanel.preferredSize.width, 700)
		explorerSplitPane.add(libraryPanel)
		explorerSplitPane.add(propertyPanel)
		explorerSplitPane.dividerLocation = BaseModule.settings.getInt("graphPanel.librarySplitPos", 700)

		bottomSidebarSplitPane.border = null
		bottomSidebarSplitPane.resizeWeight = 1.0

		bottomSidebarPane.add(issuesContent)
		bottomSidebarPane.add(logContent)

		add(titleBar, BorderLayout.NORTH)
		add(leftSidebarPane, BorderLayout.CENTER)
		add(bottomSidebarPane, BorderLayout.SOUTH)
	}

	/** Handles changes of the `isOpen` property of the [bottomSidebarPane]. */
	private fun bottomSidebarPaneChanged() {
		if (bottomSidebarPane.isOpen) {
			removeAll()
			bottomSidebarSplitPane.remove(leftSidebarPane)
			bottomSidebarSplitPane.remove(bottomSidebarPane)
			bottomSidebarSplitPane.add(leftSidebarPane)
			bottomSidebarSplitPane.add(bottomSidebarPane)
			bottomSidebarSplitPane.dividerLocation = if (bottomSidebarDividerLocation > 0) bottomSidebarDividerLocation else leftSidebarPane.height - DEF_SIDEBAR_SIZE
			bottomSidebarDividerLocation = bottomSidebarSplitPane.dividerLocation
			add(titleBar, BorderLayout.NORTH)
			add(bottomSidebarSplitPane, BorderLayout.CENTER)
		} else {
			bottomSidebarDividerLocation = bottomSidebarSplitPane.dividerLocation
			removeAll()
			bottomSidebarSplitPane.remove(leftSidebarPane)
			bottomSidebarSplitPane.remove(bottomSidebarPane)
			add(titleBar, BorderLayout.NORTH)
			add(leftSidebarPane, BorderLayout.CENTER)
			add(bottomSidebarPane, BorderLayout.SOUTH)
		}
		revalidate()
		repaint()
	}

	private fun createExecutionToolBar(controller: GraphPanelViewController): ExecutionToolbarSwing =
		ExecutionToolbarSwing(
			controller.applicationContextHolder.scheduler,
			controller.applicationContextHolder.systemSpeed,
			controller.applicationModeHolder,
			controller.toggleApplicationModeAction,
			controller.singleStepModeAction,
			controller.pauseOrResumeAction)

	private fun createDrawingToolBar(application: Application, controller: GraphPanelViewController): ToolBar {
		val toolbar = ToolBar(controller.editor)
		toolbar.addSeparator()

		toolbar.addAction(application.controller.saveAction)
		toolbar.addGap()

		toolbar.addTool(controller.editor.selectionTool, "/img/pointer24.png", Translations.getString("edit.tool.select"))
		toolbar.addTool(controller.rectangleTool, "/img/rectangle24.png", Translations.getString("edit.component.rectangle"))
		toolbar.addTool(controller.ellipseTool, "/img/oval24.png", Translations.getString("edit.component.ellipse"))
		toolbar.addTool(controller.polylineTool, "/img/polyline24.png", Translations.getString("edit.component.polyline"))
		toolbar.addTool(controller.quadCurveTool, "/img/curve24.png", Translations.getString("edit.component.quadraticCurve"))
		toolbar.addTool(controller.cubicCurveTool, "/img/cubic-curve.png", Translations.getString("edit.component.cubicCurve"))
		toolbar.addTool(controller.textTool, "/img/text24.png", Translations.getString("edit.component.text"))

		return toolbar
	}

	private fun createSettingsToolBar(controller: GraphPanelViewController): ToolBar {
		val toolBar = ToolBar(controller.editor)
		toolBar.addSeparator()

		val gridButton = JToggleButton(ActionWrapperSwing(controller.gridSnapAction))
		gridButton.text = null
		gridButton.isFocusPainted = false
		gridButton.icon = UiUtil.themedIcon("/img/grid24.png")
		gridButton.toolTipText = Translations.getString("edit.action.grid.snap.name")
		toolBar.add(gridButton)

		val button = JToggleButton(ActionWrapperSwing(controller.componentSnapAction))
		button.text = null
		button.isFocusPainted = false
		button.icon = UiUtil.themedIcon("/img/snap24.png")
		button.toolTipText = Translations.getString("edit.tool.align.name")
		toolBar.add(button)

		return toolBar
	}
}
