package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.SidebarPane
import ch.scorpion.jabbah.draw.view.ActiveViewChangedEvent
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.PropertySheetPanelFactory
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
import ch.scorpion.jabbah.execution.issue.Issue
import ch.scorpion.jabbah.execution.IssuesPanel
import ch.scorpion.jabbah.execution.PauseExecutionAction
import ch.scorpion.jabbah.execution.StepExecutionAction
import ch.scorpion.jabbah.execution.SystemSpeedSlider
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.ExecutionStoppedOnIssueEvent
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryPanel
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*


/**
 * A [JPanel] for editing and executing a root [GraphView]. It consists of a [LibraryPanel] at the left,
 * a [ComponentPropertyPanel] for editing the properties of the selected [Component] at the center-left,
 * and a [GraphNavigationPanel] for editing the [GraphView] at the center-right.
 */
class GraphPanel(
	val editor: Editor,
	val eventBus: EventBus = BaseModule.eventBus,
	val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val viewManager: ViewManager,
	graphNavigationPanelFactory: GraphNavigationPanelFactory = GraphModuleJvm.graphNavigationPanelFactory,
	var scheduler: Scheduler = ExecutionModule.scheduler,
	propertySheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel() {

	companion object {
		private val LOG by logger(GraphPanel::class)
		private const val DEF_SIDEBAR_SIZE = 200
	}

	private val graphEditPanel: GraphEditPanel = GraphEditPanel(editor, scheduler, viewManager, graphNavigationPanelFactory,
		propertySheetFactory, { eventBus.post(CloseApplicationDataRequest(editor.drawing)) }, eventBus)

	private val desktop : GraphDesktop = GraphDesktop(graphEditPanel, eventBus, viewManager, graphNavigationPanelFactory, scheduler)

	private val libraryPropertyPanel: ComponentPropertyPanel

	val libraryPanel = LibraryPanel(eventBus, libraryHolder)

	private val drawingToolBar = createDrawingToolBar()

	private val settingsToolBar = createSettingsToolBar()

	private val bottomSidebarPane = SidebarPane(SidebarPane.Orientation.Horizontal) { bottomSidebarPaneChanged() }

	val toolbars: List<ToolBar> = listOf(
		createExecutionToolBar(),
		drawingToolBar,
		settingsToolBar)

	private val librarySplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

	private val bottonSidebarSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	/** Holds the location of [bottonSidebarSplitPane]'s divider for re-establishing it the next time it opens.*/
	private var bottomSidebarDividerLocation: Int = BaseModule.settings.getInt("graphPanel.bottomSidebarSplitPos", -1)

	private var currentMode: ApplicationMode = ApplicationMode.EDIT

	/** Displays the current [Issue]s. */
	private val issuesPanel = IssuesPanel()

	private var editedGraphView: GraphView<GraphElementView<*>>? = editor.drawing as GraphView<GraphElementView<*>>?
		set(value) {
			if (field !== value) {
				val oldValue = field
				field = value
				if (value != null) {
					graphEditPanel.setGraphView(value)
					value.snapper = editor.view.grid
				}
				eventBus.post(EditedGraphViewEvent(oldValue, value))
				updateEditability()
			}
		}

	init {

		(editor.view.canvas as JComponent).transferHandler = createTransferHandler(editor, eventBus)
		libraryPropertyPanel = ComponentPropertyPanel(editor, propertySheetFactory, eventBus)

		eventBus.register(ApplicationDataEvent::class) {
			editedGraphView = (it.newData as MetaGraph?)?.graph?.graphView as GraphView<GraphElementView<*>>?
		}

		eventBus.register(ActiveViewChangedEvent::class) {
			updateEditability()
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

		buildUI()
		setMode(ApplicationMode.EDIT, true)
	}

	fun dispose() {
		BaseModule.settings.set("graphPanel.mainSplitPos", mainSplitPane.dividerLocation)
		BaseModule.settings.set("graphPanel.librarySplitPos", librarySplitPane.dividerLocation)
	}

	private fun createTransferHandler(editor: Editor, eventBus: EventBus): TransferHandler =
		GraphPanelTransferHandler(editor, eventBus, GraphElementViewTransferable.FLAVOR)

	private fun updateEditability() {
		val editable = (viewManager.activeView === editor.view && editor.view.editable)
			&& !scheduler.isActive
			&& editedGraphView != null

		editor.active = editable
		libraryPropertyPanel.editable = editable
	}

	private fun buildUI() {
		layout = BorderLayout()

		librarySplitPane.border = null
		librarySplitPane.add(libraryPanel)
		librarySplitPane.add(libraryPropertyPanel)
		librarySplitPane.dividerLocation = BaseModule.settings.getInt("graphPanel.librarySplitPos", 700)

		bottonSidebarSplitPane.border = null
		bottonSidebarSplitPane.resizeWeight = 1.0

		bottomSidebarPane.add(Translations.getString("graph.issues.title"), "/img/issue-16.png", issuesPanel)

		mainSplitPane.add(librarySplitPane)
		mainSplitPane.add(desktop)
		mainSplitPane.dividerLocation = BaseModule.settings.getInt("graphPanel.mainSplitPos", 250)
		mainSplitPane.border = null

		add(mainSplitPane, BorderLayout.CENTER)
		add(bottomSidebarPane, BorderLayout.SOUTH)
	}

	/** Handles changes of the �isOpen� property of the [bottomSidebarPane]. */
	private fun bottomSidebarPaneChanged() {
		if (bottomSidebarPane.isOpen) {
			removeAll()
			bottonSidebarSplitPane.remove(mainSplitPane)
			bottonSidebarSplitPane.remove(bottomSidebarPane)
			bottonSidebarSplitPane.add(mainSplitPane)
			bottonSidebarSplitPane.add(bottomSidebarPane)
			bottonSidebarSplitPane.dividerLocation = if (bottomSidebarDividerLocation > 0) bottomSidebarDividerLocation else mainSplitPane.height - DEF_SIDEBAR_SIZE
			bottomSidebarDividerLocation = bottonSidebarSplitPane.dividerLocation
			add(bottonSidebarSplitPane, BorderLayout.CENTER)
		} else {
			bottomSidebarDividerLocation = bottonSidebarSplitPane.dividerLocation
			removeAll()
			bottonSidebarSplitPane.remove(mainSplitPane)
			bottonSidebarSplitPane.remove(bottomSidebarPane)
			add(mainSplitPane, BorderLayout.CENTER)
			add(bottomSidebarPane, BorderLayout.SOUTH)
		}
		revalidate()
		repaint()
	}

	private fun setMode(mode: ApplicationMode, init: Boolean) {
		currentMode = mode

		when (currentMode) {
			ApplicationMode.EDIT -> {
				if (!init) {
					scheduler.isActive = false
				}
				updateEditability()
				eventBus.post(ApplicationModeEvent(currentMode))
			}
			ApplicationMode.EXECUTE -> {
				issuesPanel.clear()
				if ((editor.drawing as GraphView<*>).checkDesign()) {
					editor.view.selectionManager.deselectAll()
					InvocationHandler.invoke(Runnable {
						scheduler.isActive = true
						updateEditability()
						eventBus.post(ApplicationModeEvent(currentMode))
					})
				} else {
					eventBus.post(ComponentMessage(type = ComponentMessageType.Error, source = null, messageKey = "graph.designError.msg"))
					LOG.debug("GraphPanel: execution not started due to design errors")
				}
			}
		}
	}

	private fun createExecutionToolBar(): ToolBar {
		val modeToggleAction = ToggleModeAction(scheduler, eventBus)
		val modeToggleButton = JToggleButton(modeToggleAction)
		modeToggleButton.text = null
		modeToggleButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/powerOff-24.png"))
		modeToggleButton.toolTipText = Translations.getString("simulation.action.execute.name")

		val executionAction = PauseExecutionAction(scheduler, eventBus)
		val pauseToggleButton = JToggleButton(ActionWrapperSwing(executionAction))
		pauseToggleButton.text = null
		pauseToggleButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/PauseOff-24.png"))
		pauseToggleButton.toolTipText = executionAction.name

		val stepButton = JButton(ActionWrapperSwing(StepExecutionAction(scheduler, eventBus)))
		stepButton.text = null
		stepButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/Resume-24.png"))
		stepButton.preferredSize = Dimension(40, 40)

		val speedSlider = SystemSpeedSlider()
		speedSlider.maximumSize = Dimension(200, speedSlider.maximumSize.height)

		val mainToolBar = ToolBar()
		mainToolBar.isFloatable = false
		mainToolBar.isRollover = true
		mainToolBar.addSeparator()
		mainToolBar.add(modeToggleButton)
		mainToolBar.add(pauseToggleButton)
		mainToolBar.add(stepButton)
		mainToolBar.add(speedSlider)

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
		val action = ToggleComponentSnapAction()
		val button = JToggleButton(action)
		button.text = null
		button.isFocusPainted = false
		button.icon = ImageIcon(GraphPanel::class.java.getResource("/img/snap.gif"))
		button.toolTipText = Translations.getString("edit.tool.align.name")

		toolBar.add(button)

		return toolBar
	}

	/** Toggles a [Scheduler] on and off.*/
	private inner class ToggleModeAction(private val scheduler: Scheduler, eventBus: EventBus) : AbstractAction() {
		init {
			updateState()
			eventBus.register(SchedulerActivationStateEvent::class, { updateState() })

		}

		override fun actionPerformed(e: ActionEvent?) {
			if (scheduler.isActive) {
				setMode(ApplicationMode.EDIT, false)
			} else {
				setMode(ApplicationMode.EXECUTE, false)
			}
		}

		private fun updateState() {
			putValue(Action.SELECTED_KEY, scheduler.isActive)
		}
	}

	private inner class ToggleComponentSnapAction : AbstractAction() {
		init {
			updateState()
			editor.addPropertyChangeListener(object : PropertyChangeListener<Any> {
				override fun propertyChanged(e: PropertyChangeEvent<Any>) {
					if (e.name == Editor.PROP_COMPONENT_SNAP) {
						updateState()
					} else if (e.name == Editor.PROP_ACTIVE) {
						isEnabled = editor.active
					}
				}
			})
		}

		override fun actionPerformed(e: ActionEvent?) {
			editor.componentSnap = !editor.componentSnap
		}

		private fun updateState() {
			putValue(Action.SELECTED_KEY, editor.componentSnap)
		}
	}
}

/** Posted on [EventBus] when the currently (one and only) edited root [GraphView] changes. */
class EditedGraphViewEvent(val oldGraphView: GraphView<GraphElementView<*>>?, val newGraphView: GraphView<GraphElementView<*>>?)
