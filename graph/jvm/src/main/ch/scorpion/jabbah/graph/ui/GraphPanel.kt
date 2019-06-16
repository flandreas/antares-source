package ch.scorpion.jabbah.graph.ui

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
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.view.ActiveViewChangedEvent
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
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
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.view.AttentionDrawer
import ch.scorpion.jabbah.execution.IssuesPanel
import ch.scorpion.jabbah.execution.PauseExecutionAction
import ch.scorpion.jabbah.execution.StepExecutionAction
import ch.scorpion.jabbah.execution.SystemSpeedSlider
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
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseSelector
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.graph.view.GraphView
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
	val eventBus: EventBus = BaseModule.eventBus,
	val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val viewManager: ViewManager,
	graphNavigationPanelFactory: GraphNavigationPanelFactory = GraphModuleJvm.graphNavigationPanelFactory,
	var scheduler: Scheduler = ExecutionModule.scheduler,
	propertySheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory,
	showContentInitially: Boolean = true
) : JPanel(), ApplicationModeHolder {

	companion object {
		private val LOG by logger(GraphPanel::class)
		private const val DEF_SIDEBAR_SIZE = 200
	}

	/** Allows to edit and execute the currently open GraphView.*/
	private val graphEditPanel: GraphEditPanel = GraphEditPanel(editor, scheduler, viewManager, graphNavigationPanelFactory,
		propertySheetFactory, eventBus)

	/** Allows to open multiple Graphs.*/
	val desktop: GraphDesktop = GraphDesktop(graphEditPanel, eventBus, scheduler, showContentInitially)

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
		settingsToolBar)

	/** The "Explorer" contains [libraryPanel] and [propertyPanel].*/
	private val explorerSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	/**
	 * Allows to show and hide the Explorer. If not initialy opened, the event system turns crazy and lags while processing
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
		issuesPanel)

	var rootGraphView: GraphView<GraphElementView<*>>? = editor.drawing as GraphView<GraphElementView<*>>?
		private set(value) {
			if (field !== value) {
				val oldValue = field
				field = value
				if (value != null) {
					graphEditPanel.setGraphView(value)
					value.snapper = editor.view.grid
				}
				eventBus.post(EditedGraphViewEvent(this, oldValue, value))
				updateEditability()
			}
		}

	init {

		(editor.view.canvas as JComponent).transferHandler = createTransferHandler(editor, eventBus)
		propertyPanel = ComponentPropertyPanel(editor, propertySheetFactory, eventBus)

		eventBus.register(ApplicationDataEvent::class) {
			rootGraphView = (it.newData as MetaGraph?)?.graph?.graphView as GraphView<GraphElementView<*>>?
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
		graphEditPanel.dispose()
		BaseModule.settings.set("graphPanel.librarySplitPos", explorerSplitPane.dividerLocation)
	}

	private fun createTransferHandler(editor: Editor, eventBus: EventBus): TransferHandler =
		GraphPanelTransferHandler(editor, eventBus, GraphElementViewTransferable.FLAVOR)

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
				if ((editor.drawing as GraphView<*>).checkDesign()) {
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
		modeToggleButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/powerOff-24.png"))
		modeToggleButton.toolTipText = Translations.getString("simulation.action.execute.name")

		val executionAction = PauseExecutionAction(scheduler, eventBus)
		val pauseToggleButton = JToggleButton(ActionWrapperSwing(executionAction))
		pauseToggleButton.text = null
		pauseToggleButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/PauseOff-24.png"))
		pauseToggleButton.toolTipText = executionAction.name

		val stepButton = StepButton("/img/Resume-24.png", StepExecutionAction(scheduler, eventBus))

		val speedSlider = SystemSpeedSlider()
		speedSlider.maximumSize = Dimension(200, speedSlider.maximumSize.height)

		val usecaseSelector = UsecaseSelector()

		val mainToolBar = ToolBar()
		mainToolBar.isFloatable = false
		mainToolBar.isRollover = true
		mainToolBar.addSeparator()
		mainToolBar.add(modeToggleButton)
		mainToolBar.add(pauseToggleButton)
		mainToolBar.add(stepButton)
		mainToolBar.add(speedSlider)
		mainToolBar.add(usecaseSelector)

		return mainToolBar
	}

	private fun createDrawingToolBar(): ToolBar {
		val toolbar = ToolBar(editor)
		toolbar.addSeparator()

		toolbar.addTool(editor.currentTool, "/img/pointer.gif", Translations.getString("edit.tool.select"))
		toolbar.addTool(RectangleTool(editor, { RectangleComponent() }, { GraphElementViewWrapper<Vertice>(it) }),
			"/img/rectangle.png", Translations.getString("edit.component.rectangle"))
		toolbar.addTool(RectangleTool(editor, { EllipseComponent() }, { GraphElementViewWrapper<Vertice>(it) }),
			"/img/ellipse.png", Translations.getString("edit.component.ellipse"))
		toolbar.addTool(PolylineTool(editor, { PolylineComponent() }, { GraphElementViewWrapper<Vertice>(it) }),
			"/img/polyline.gif", Translations.getString("edit.component.polyline"))
		toolbar.addTool(QuadCurveTool(editor, { QuadCurveComponent() }, { GraphElementViewWrapper<Vertice>(it) }),
			"/img/curve-20.png", Translations.getString("edit.component.quadraticCurve"))
		toolbar.addTool(TextTool(editor, { TextComponentJvm("Text") }, { GraphElementViewWrapper<Vertice>(it) }),
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

	private class StepButton(iconPath: String, private val action: Action) : JPanel() {

		private val button = JButton(ActionWrapperSwing(action))
		private val color = Graphics2DJvm.toAwtColor(BaseModule.properties.get<Color>(AttentionDrawer.PROP_COLOR))

		init {
			button.text = null
			button.icon = ImageIcon(GraphPanel::class.java.getResource(iconPath))
			button.preferredSize = Dimension(40, 40)

			layout = BoxLayout(this, BoxLayout.LINE_AXIS)

			add(button)

			action.addPropertyChangeListener(object : PropertyChangeListener<Any> {
				override fun propertyChanged(e: PropertyChangeEvent<Any>) {
					background = if (action.enabled) color else null
				}
			})
		}
	}
}

/** Posted on [EventBus] when the currently (one and only) edited root [GraphView] changes. */
class EditedGraphViewEvent(
	val applicationModeHolder: ApplicationModeHolder,
	val oldGraphView: GraphView<GraphElementView<*>>?,
	val newGraphView: GraphView<GraphElementView<*>>?
)
