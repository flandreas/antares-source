package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.SidebarPane
import ch.scorpion.jabbah.base.swing.SidebarPaneContentImpl
import ch.scorpion.jabbah.base.swing.SidebarSplitPane
import ch.scorpion.jabbah.draw.view.ActiveViewChangedEvent
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.app.ComponentSnapAction
import ch.scorpion.jabbah.edit.app.GridSnapAction
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.QuadCurveTool
import ch.scorpion.jabbah.edit.model.curve.QuadCurveComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineTool
import ch.scorpion.jabbah.edit.model.rectangle.EllipseComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleTool
import ch.scorpion.jabbah.edit.model.text.TextComponentJvm
import ch.scorpion.jabbah.edit.model.text.TextTool
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.execution.*
import ch.scorpion.jabbah.execution.issue.Issue
import ch.scorpion.jabbah.execution.issue.IssueCollectorEvent
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.ExecutionStoppedOnIssueEvent
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.ApplicationModeHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryPanel
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseSelector
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.GraphViewAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*


/**
 * A [JPanel] for editing and executing a root [GraphView].
 *
 * It consists of a [LibraryPanel] at the left, a [ComponentPropertyPanel] for editing the properties
 * of the selected [Component] at the center-left, and a [GraphNavigationPanel] for editing the [GraphView]
 * at the center-right.
 */
class GraphPanel(
	val editor: Editor,
	val graphViewAppService: GraphViewAppService = GraphViewModule.graphViewAppService,
	val eventBus: EventBus = BaseModule.eventBus,
	val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val viewManager: ViewManager,
	var scheduler: Scheduler = ExecutionModule.scheduler,
	application: Application,
	propertySheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel(), ApplicationModeHolder {

	companion object {
		private val LOG by logger(GraphPanel::class)
		private const val DEF_SIDEBAR_SIZE = 200
	}

	/** Allows editing and execute the currently open GraphView.*/
	private val graphEditPanel: GraphEditPanel = GraphEditPanel(application, editor, scheduler, viewManager, propertySheetFactory, eventBus)

	val desktopController = GraphDesktopController()

	/** Allows opening multiple Graphs.*/
	val desktop: GraphDesktopSwing = GraphDesktopSwing(graphEditPanel)

	/** Displays the properties of the currently selected component in [graphEditPanel].*/
	private val propertyPanel: ComponentPropertyPanel

	/** Contains UI for selecting components from the current library or the current project.*/
	val libraryPanel = LibraryPanel(eventBus, libraryHolder)

	private val drawingToolBar = createDrawingToolBar()

	private val settingsToolBar = createSettingsToolBar()

	/** Contains the errors view.*/
	private val bottomSidebarPane = SidebarPane(SidebarPane.Location.Bottom) { bottomSidebarPaneChanged() }

	val toolbars: List<ToolBar> = listOf(
		createExecutionToolBar(),
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
			ImageIcon(SidebarPane::class.java.getResource("/img/compass-16.png")),
			explorerSplitPane)
		))

	private val bottomSidebarSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	/** Holds the location of [bottomSidebarSplitPane]'s divider for re-establishing it the next time it opens.*/
	private var bottomSidebarDividerLocation: Int = BaseModule.settings.getInt("graphPanel.bottomSidebarSplitPos", -1)

	override var currentMode: ApplicationMode = ApplicationMode.EDIT
		private set

	/** Displays the current [Issue]s. */
	private val issuesPanel = IssuesPanel()

	private val issuesContent = SidebarPaneContentImpl(
		Translations.getString("graph.issues.title"),
		ImageIcon(GraphPanel::class.java.getResource("/img/issue-16.png")),
		issuesPanel,
		listOf(ClearIssuesPanelAction(issuesPanel)))

	private val logPanel = LogPanel()

	private val logContent = SidebarPaneContentImpl(
		Translations.getString("graph.log.title"),
		ImageIcon(GraphPanel::class.java.getResource("/img/log-16.png")),
		logPanel,
		listOf(ClearLogPanelAction(logPanel)))

	private var rootGraphView: GraphView? = editor.drawing as GraphView?

	val showsNavigationRoot: Boolean get() = graphEditPanel.graphNavigationPanel.showsNavigationRoot

	init {

		desktopController.view = desktop

		(editor.view.canvas as JComponent).transferHandler = createTransferHandler(editor, eventBus)
		propertyPanel = ComponentPropertyPanel(editor, propertySheetFactory, eventBus)

		eventBus.register(ApplicationDataEvent::class) {
			setApplicationData((it.newData?.content as MetaGraph?)?.graph?.graphView)
		}

		eventBus.register(ApplicationDataContentEvent::class) {
			setApplicationDataContent((it.data.content as MetaGraph?)?.graph?.graphView)
		}

		eventBus.register(ActiveViewChangedEvent::class) {
			updateEditability()
		}

		eventBus.register(SchedulerActivationStateEvent::class) {
			if (!it.scheduler.isActive) {
				setMode(ApplicationMode.EDIT)
			}
		}

		eventBus.register(ExecutionStoppedOnIssueEvent::class) {
			eventBus.post(ComponentMessage(
				type = ComponentMessageType.Error,
				source = null,
				messageKey = "execution.scheduler.stoppedDueToIssue.msg"))
		}

		editor.view.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				if (e.name == DrawingView.PROP_EDITABLE) {
					updateEditability()
				}
			}
		})

		eventBus.register(IssueCollectorEvent::class) {
			val iconPath = when (it.issueCollector.maximumSeverity) {
				null -> "/img/issue-16.png"
				IssueSeverity.Warning -> "/img/warning-16.png"
				IssueSeverity.Error -> "/img/error-16.png"
			}
			issuesContent.icon = ImageIcon(GraphPanel::class.java.getResource(iconPath))
		}

		buildUI()
		setMode(ApplicationMode.EDIT, true)
	}

	fun dispose() {
		leftSidebarPane.dispose()
		issuesPanel.dispose()
		logPanel.dispose()
		graphEditPanel.dispose()
		BaseModule.settings.set("graphPanel.librarySplitPos", explorerSplitPane.dividerLocation)
	}

	private fun setApplicationData(graphView: GraphView?) {
		if (rootGraphView != graphView) {
			desktopController.showMainOnly()
			System.invokeLater {
				// This will apply the Zoom strategy, which requires that the main Swing UI has already been laid out
				setRootGraphView(graphView, applyZoomStrategy = true)
			}
		}
	}

	/**
	 * This is primarily called when the states is replayed from undoable history, and the undoable commands are
	 * replayed immediately after the the new [GraphView] has been set, which is why invoking this later would not work.
	 */
	private fun setApplicationDataContent(graphView: GraphView?) {
		if (rootGraphView != graphView) {
			setRootGraphView(graphView, applyZoomStrategy = false)
		}
	}

	private fun setRootGraphView(graphView: GraphView?, applyZoomStrategy: Boolean) {
		val oldValue = rootGraphView
		rootGraphView = graphView
		rootGraphView?.let {
			graphEditPanel.setGraphView(it, applyZoomStrategy)
			it.snapper = editor.view.grid
		}
		eventBus.post(EditedGraphViewEvent(this, oldValue, rootGraphView))
		updateEditability()
	}

	private fun createTransferHandler(editor: Editor, eventBus: EventBus): TransferHandler =
		GraphPanelTransferHandler(graphViewAppService, editor, eventBus, GraphElementViewTransferable.FLAVOR)

	private fun updateEditability() {
		val editable = (viewManager.activeView === editor.view && editor.view.editable)
			&& !scheduler.isActive
			&& rootGraphView != null

		editor.active = editable
		propertyPanel.editable = editable
	}

	private fun buildUI() {
		layout = BorderLayout()

		explorerSplitPane.border = null
		explorerSplitPane.add(libraryPanel)
		explorerSplitPane.add(propertyPanel)
		explorerSplitPane.dividerLocation = BaseModule.settings.getInt("graphPanel.librarySplitPos", 700)

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

	private fun setMode(mode: ApplicationMode, init: Boolean, after: () -> Unit = {}) {
		if (mode == currentMode) {
			return
		}
		when (mode) {
			ApplicationMode.EDIT -> {
				currentMode = mode
				if (!init) {
					scheduler.isActive = false
				}
				updateEditability()
				eventBus.post(ApplicationModeEvent(currentMode))
				Status.set(StatusType.Large, Translations.getString("graph.status.edit"))
			}
			ApplicationMode.EXECUTE, ApplicationMode.EXEC_USECASE -> {
				issuesPanel.clear()
				if ((editor.drawing as GraphView).checkDesign()) {
					currentMode = mode
					graphEditPanel.graphNavigationPanel.deselectAll()
					InvocationHandler.invoke(Runnable {
						scheduler.isActive = true
						updateEditability()
						eventBus.post(ApplicationModeEvent(currentMode))
						Status.set(StatusType.Large, Translations.getString("graph.status.execute"))
						after.invoke()
					})
				} else {
					eventBus.post(ComponentMessage(type = ComponentMessageType.Error, source = null, messageKey = "graph.designError.msg"))
					LOG.debug("execution not started due to design errors")
				}
			}
		}
	}

	override fun setMode(mode: ApplicationMode, after: () -> Unit) {
		setMode(mode, init = false, after = after)
	}

	private fun createExecutionToolBar(): ToolBar {
		val modeToggleAction = ActionWrapperSwing(ToggleApplicationModeAction())
		val modeToggleButton = JToggleButton(modeToggleAction)
		modeToggleButton.text = null
		modeToggleButton.hideActionText = true
		modeToggleButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/play24.png"))
		modeToggleButton.toolTipText = Translations.getString("execution.action.execute.name")

		val executionAction = PauseExecutionAction(scheduler, eventBus)
		val pauseToggleButton = JToggleButton(ActionWrapperSwing(executionAction))
		pauseToggleButton.text = null
		pauseToggleButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/pause24.png"))
		pauseToggleButton.toolTipText = executionAction.name

		//val stepButton = StepButton("/img/Resume-24.png", StepExecutionAction(scheduler, eventBus))

		val speedSlider = SystemSpeedSlider()
		speedSlider.maximumSize = Dimension(200, speedSlider.maximumSize.height)

		val usecaseSelector = UsecaseSelector()

		val mainToolBar = ToolBar()
		mainToolBar.isFloatable = false
		mainToolBar.isRollover = true
		mainToolBar.addSeparator()
		mainToolBar.add(modeToggleButton)
		mainToolBar.add(pauseToggleButton)
		mainToolBar.add(createStepButton(StepExecutionAction(scheduler, eventBus)))
		mainToolBar.add(speedSlider)
		mainToolBar.add(usecaseSelector)

		return mainToolBar
	}

	private fun createStepButton(action: Action): JButton {
		val inactiveIcon = ImageIcon(GraphPanel::class.java.getResource("/img/resume24.png"))
		val activeIcon = ImageIcon(GraphPanel::class.java.getResource("/img/resume-active24.png"))
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

	private fun createDrawingToolBar(): ToolBar {
		val toolbar = ToolBar(editor)
		toolbar.addSeparator()

		toolbar.addTool(editor.currentTool, "/img/pointer.gif", Translations.getString("edit.tool.select"))
		toolbar.addTool(RectangleTool(editor, factory = { RectangleComponent() }, adder = { GraphElementViewWrapper(it) }),
			"/img/rectangle.png", Translations.getString("edit.component.rectangle"))
		toolbar.addTool(RectangleTool(editor, factory = { EllipseComponent() }, adder = { GraphElementViewWrapper(it) }),
			"/img/ellipse.png", Translations.getString("edit.component.ellipse"))
		toolbar.addTool(PolylineTool(editor, factory = { PolylineComponent() }, adder = { GraphElementViewWrapper(it) }),
			"/img/polyline.gif", Translations.getString("edit.component.polyline"))
		toolbar.addTool(QuadCurveTool(editor, factory = { QuadCurveComponent() }, adder = { GraphElementViewWrapper(it) }),
			"/img/curve-20.png", Translations.getString("edit.component.quadraticCurve"))
		toolbar.addTool(TextTool(editor,factory =  { TextComponentJvm(TranslatableText("Text")) }, adder = { GraphElementViewWrapper(it) }),
			"/img/text.gif", Translations.getString("edit.component.text"))

		return toolbar
	}

	private fun createSettingsToolBar(): ToolBar {
		val toolBar = ToolBar(editor)
		toolBar.addSeparator()

		val gridButton = JToggleButton(ActionWrapperSwing(GridSnapAction(editor)))
		gridButton.text = null
		gridButton.isFocusPainted = false
		gridButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/snapGrid.gif"))
		gridButton.toolTipText = Translations.getString("edit.action.grid.snap.name")
		toolBar.add(gridButton)

		val button = JToggleButton(ActionWrapperSwing(ComponentSnapAction(editor)))
		button.text = null
		button.isFocusPainted = false
		button.icon = ImageIcon(GraphPanel::class.java.getResource("/img/snap.gif"))
		button.toolTipText = Translations.getString("edit.tool.align.name")
		toolBar.add(button)

		return toolBar
	}
}
