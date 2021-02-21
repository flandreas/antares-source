package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.SidebarPane
import ch.scorpion.jabbah.base.swing.SidebarPaneContentImpl
import ch.scorpion.jabbah.base.swing.SidebarSplitPane
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.execution.IssuesViewSwing
import ch.scorpion.jabbah.execution.PauseExecutionAction
import ch.scorpion.jabbah.execution.ResumeExecutionAction
import ch.scorpion.jabbah.execution.SystemSpeedSliderSwing
import ch.scorpion.jabbah.execution.issue.Issue
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.library.LibraryPanelSwing
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewController
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseSelector
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.GraphViewAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.*


/**
 * A [javax.swing] implementation of [GraphPanelView]
 */
class GraphPanelViewSwing(
	controller: GraphPanelViewController,
	private val graphViewAppService: GraphViewAppService = GraphViewModule.graphViewAppService,
	private val eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager,
	private var scheduler: Scheduler = ExecutionModule.scheduler,
	application: Application,
	propertySheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel(), GraphPanelView {

	companion object {
		private const val DEF_SIDEBAR_SIZE = 200
	}

	/** Allows editing and execute the currently open GraphView.*/
	private val graphEditView: GraphEditViewSwing = GraphEditViewSwing(controller.editViewController, application, controller.editor, viewManager, propertySheetFactory, eventBus)

	/** Allows opening multiple Graphs.*/
	private val desktop: GraphDesktopViewSwing = GraphDesktopViewSwing(controller.desktopController, graphEditView)

	/** Displays the properties of the currently selected component in [graphEditView].*/
	private val propertyPanel: ComponentPropertyPanel

	/** Contains UI for selecting components from the current library or the current project.*/
	val libraryPanel = LibraryPanelSwing(controller.libraryPanelController, application, eventBus)

	private val drawingToolBar = createDrawingToolBar(controller)

	private val settingsToolBar = createSettingsToolBar(controller)

	/** Contains the errors view.*/
	private val bottomSidebarPane = SidebarPane(SidebarPane.Location.Bottom) { bottomSidebarPaneChanged() }

	val toolbars: List<ToolBar> = listOf(
		createExecutionToolBar(controller),
		drawingToolBar,
		settingsToolBar).onEach { it.isFloatable = false }

	/** The "Explorer" contains [libraryPanel] and [propertyPanel].*/
	private val explorerSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	/**
	 * Allows showing and hiding the Explorer. If not initially opened, the event system turns crazy and lags while processing
	 * drag events in the DrawingView (BUG still not explained).
	 */
	private val leftSidebarPane = SidebarSplitPane(
		location = SidebarPane.Location.Left,
		mainContent = desktop,
		settingBaseName = "graphPanel.leftSidebar",
		providedInitialOpenIndex = 0,
		contents = listOf(SidebarPaneContentImpl(
			Translations.getString("graph.explorer.name"),
			UiUtil.themedIcon("/img/compass-16.png"),
			explorerSplitPane)
		))

	private val bottomSidebarSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	/** Holds the location of [bottomSidebarSplitPane]'s divider for re-establishing it the next time it opens.*/
	private var bottomSidebarDividerLocation: Int = BaseModule.settings.getInt("graphPanel.bottomSidebarSplitPos", -1)

	/** Displays the current [Issue]s. */
	private val issuesPanel = IssuesViewSwing(controller.issuesViewController)

	private val issuesContent = SidebarPaneContentImpl(
		Translations.getString("graph.issues.title"),
		UiUtil.themedIcon("/img/issue-16.png"),
		issuesPanel,
		listOf(controller.issuesViewController.clearAction))

	private val logPanel = LogViewSwing(controller.logViewController)

	private val logContent = SidebarPaneContentImpl(
		Translations.getString("graph.log.title"),
		UiUtil.themedIcon("/img/log-16.png"),
		logPanel,
		listOf(controller.logViewController.clearAction))

	val showsNavigationRoot: Boolean get() = graphEditView.graphNavigationView.showsNavigationRoot

	init {
		controller.view = this

		(controller.editor.view.canvas as JComponent).transferHandler = createTransferHandler(controller.editor, eventBus)
		propertyPanel = ComponentPropertyPanel(controller.editor, propertySheetFactory, eventBus)

		buildUI()
		controller.editViewController.setGraphView(controller.editor.drawing as GraphView, true)
	}

	override fun dispose() {
		leftSidebarPane.dispose()
		issuesPanel.dispose()
		logPanel.dispose()
		propertyPanel.dispose()

		BaseModule.settings.set("graphPanel.librarySplitPos", explorerSplitPane.dividerLocation)
	}

	override var maxIssueSeverity: IssueSeverity? = null
		set(value) {
			field = value
			val iconPath = when (value) {
				null -> "/img/issue-16.png"
				IssueSeverity.Warning -> "/img/warning-16.png"
				IssueSeverity.Error -> "/img/error-16.png"
			}
			issuesContent.icon = UiUtil.themedIcon(iconPath)
		}

	private fun createTransferHandler(editor: Editor, eventBus: EventBus): TransferHandler =
		GraphPanelTransferHandler(graphViewAppService, editor, eventBus, GraphElementViewTransferable.FLAVOR)

	private fun buildUI() {
		layout = BorderLayout()

		explorerSplitPane.border = null
		explorerSplitPane.add(libraryPanel)
		explorerSplitPane.add(propertyPanel)
		explorerSplitPane.dividerLocation = BaseModule.settings.getInt("graphPanel.librarySplitPos", Toolkit.getDefaultToolkit().screenSize.height / 2)

		bottomSidebarSplitPane.border = null
		bottomSidebarSplitPane.resizeWeight = 1.0

		bottomSidebarPane.add(issuesContent)
		bottomSidebarPane.add(logContent)

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
			add(bottomSidebarSplitPane, BorderLayout.CENTER)
		} else {
			bottomSidebarDividerLocation = bottomSidebarSplitPane.dividerLocation
			removeAll()
			bottomSidebarSplitPane.remove(leftSidebarPane)
			bottomSidebarSplitPane.remove(bottomSidebarPane)
			add(leftSidebarPane, BorderLayout.CENTER)
			add(bottomSidebarPane, BorderLayout.SOUTH)
		}
		revalidate()
		repaint()
	}

	private fun createExecutionToolBar(controller: GraphPanelViewController): ToolBar {
		val modeToggleAction = ActionWrapperSwing(controller.toggleApplicationModeAction)
		val modeToggleButton = JToggleButton(modeToggleAction)
		modeToggleButton.text = null
		modeToggleButton.hideActionText = true
		modeToggleButton.icon = UiUtil.themedIcon("/img/play24.png")
		modeToggleButton.toolTipText = Translations.getString("execution.action.execute.name")

		val pauseAction = PauseExecutionAction(scheduler, eventBus)
		val pauseToggleButton = JToggleButton(ActionWrapperSwing(pauseAction))
		pauseToggleButton.text = null
		pauseToggleButton.icon = UiUtil.themedIcon("/img/pause24.png")
		pauseToggleButton.toolTipText = pauseAction.name

		val speedSlider = SystemSpeedSliderSwing()
		speedSlider.maximumSize = Dimension(200, speedSlider.maximumSize.height)

		val usecaseSelector = UsecaseSelector()

		val mainToolBar = ToolBar()
		mainToolBar.isFloatable = false
		mainToolBar.isRollover = true
		mainToolBar.addSeparator()
		mainToolBar.add(modeToggleButton)
		mainToolBar.add(pauseToggleButton)
		mainToolBar.add(createStepButton(ResumeExecutionAction(scheduler, eventBus)))
		mainToolBar.add(speedSlider)
		mainToolBar.add(usecaseSelector)

		return mainToolBar
	}

	private fun createStepButton(action: Action): JButton {
		val inactiveIcon = UiUtil.themedIcon("/img/resume24.png")
		val activeIcon = ImageIcon(GraphPanelViewSwing::class.java.getResource("/img/resume-active24.png"))
		val button = JButton(ActionWrapperSwing(action))
		button.text = null
		button.icon = inactiveIcon

		action.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				if (e.name == Action.PROP_ENABLED) {
					button.icon = if (action.enabled) {
						activeIcon
					} else {
						inactiveIcon
					}
				}
			}
		})

		return button
	}

	private fun createDrawingToolBar(controller: GraphPanelViewController): ToolBar {
		val toolbar = ToolBar(controller.editor)
		toolbar.addSeparator()

		toolbar.addTool(controller.editor.selectionTool, "/img/pointer24.png", Translations.getString("edit.tool.select"))
		toolbar.addTool(controller.rectangleTool, "/img/rectangle24.png", Translations.getString("edit.component.rectangle"))
		toolbar.addTool(controller.ellipseTool, "/img/oval24.png", Translations.getString("edit.component.ellipse"))
		toolbar.addTool(controller.polylineTool, "/img/polyline24.png", Translations.getString("edit.component.polyline"))
		toolbar.addTool(controller.quadCurveTool, "/img/curve24.png", Translations.getString("edit.component.quadraticCurve"))
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
