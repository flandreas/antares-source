package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.invocation.InvocationHandler
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
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryPanel
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioPanel
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
        val eventBus: EventBus,
        val libraryHolder: LibraryHolder,
        private val viewManager: ViewManager,
        graphNavigationPanelFactory: GraphNavigationPanelFactory,
        var scheduler: Scheduler,
        propertySheetFactory: PropertySheetPanelFactory
) : JPanel() {

    companion object {
        private val DEF_SIDEBAR_SIZE = 200
    }

    constructor(editor: Editor, viewManager: ViewManager): this(
            editor,
            BaseModule.eventBus,
            LibraryModule.libraryHolder,
            viewManager,
            GraphModuleJvm.graphNavigationPanelFactory,
            ExecutionModule.scheduler,
            EditModuleJvm.propertySheetPanelFactory)

    private val modeToggleAction = ToggleModeAction(scheduler, eventBus)

    private val mainPanel = JPanel(BorderLayout())

    private val scenarioPanel = ScenarioPanel(editor, eventBus, propertySheetFactory)

    private val libraryPropertyPanel: ComponentPropertyPanel

    val libraryPanel = LibraryPanel(eventBus, libraryHolder)

    val graphNavigationPanel = graphNavigationPanelFactory.create(
        isRoot = true,
        drawingView = editor.view as DrawingView<GraphView<GraphElementView<*>>>,
        viewManager = viewManager,
        closeHandler = null,
        contextColor = null,
        scheduler = scheduler)

    private val drawingToolBar = createDrawingToolBar()

    private val settingsToolBar = createSettingsToolBar()

    private val rightSidebarPane: SidebarPane = SidebarPane(SidebarPane.Orientation.Vertical, { rightSidebarPaneChanged() })

    private val bottomSidebarPane = SidebarPane(SidebarPane.Orientation.Horizontal, { bottomSidebarPaneChanged() })

    val toolbars: List<JToolBar> = listOf(
            createExecutionToolBar(),
            drawingToolBar,
            settingsToolBar)

    private val librarySplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

    private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

    private val rightSidebarSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

    private val bottonSidebarSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

    /** Holds the location of [rightSidebarSplitPane]'s divider for re-establishing it the next time it opens.*/
    private var rightSidebarDividerLocation: Int = BaseModule.settings.getInt("graphPanel.rightSidebarSplitPos", -1)

    /** Holds the location of [bottonSidebarSplitPane]'s divider for re-establishing it the next time it opens.*/
    private var bottomSidebarDividerLocation: Int = BaseModule.settings.getInt("graphPanel.bottomSidebarSplitPos", -1)

    private var currentMode: ApplicationMode = ApplicationMode.EDIT

    /** Displays the current [Issue]s. */
    private val issuesPanel = IssuesPanel()

    init {
        (editor.view.canvas as JComponent).transferHandler = createTransferHandler(editor, eventBus)
        libraryPropertyPanel = ComponentPropertyPanel(editor, propertySheetFactory, eventBus)

        eventBus.register(ActiveViewChangedEvent::class, { updateEditability() })
        eventBus.register(ExecutionStoppedOnIssueEvent::class, {
            eventBus.post(ComponentMessage(
                    type = ComponentMessageType.Error,
                    source = null,
                    messageKey = "execution.scheduler.stoppedDueToIssue.msg"))
        })

        editor.view.addPropertyChangeListener(object : PropertyChangeListener<Any>{
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
        BaseModule.settings.set("graphPanel.sidebarSplitPos", rightSidebarSplitPane.dividerLocation)
    }

    private fun createTransferHandler(editor: Editor, eventBus: EventBus): TransferHandler =
            GraphPanelTransferHandler(editor, eventBus, GraphElementViewTransferable.FLAVOR, libraryHolder)

    fun setGraphView(newGraphView: GraphView<GraphElementView<*>>) {
        val oldGraphView = graphNavigationPanel.drawingView.drawing
        graphNavigationPanel.setRootGraphView(newGraphView)
        scenarioPanel.graphView = newGraphView
        eventBus.post(EditedGraphViewEvent(oldGraphView, newGraphView))
    }

    private fun updateEditability() {
        val editable = viewManager.activeView === editor.view && editor.view.editable
        drawingToolBar.isEnabled = editable
        settingsToolBar.isEnabled = editable
        editor.active = editable && scheduler.isActive == false
    }

    private fun buildUI() {
        layout = BorderLayout()

        librarySplitPane.border = null
        librarySplitPane.add(libraryPanel)
        librarySplitPane.add(libraryPropertyPanel)
        librarySplitPane.dividerLocation = BaseModule.settings.getInt("graphPanel.librarySplitPos", 700)

        rightSidebarSplitPane.border = null
        rightSidebarSplitPane.resizeWeight = 1.0

        bottonSidebarSplitPane.border = null
        bottonSidebarSplitPane.resizeWeight = 1.0

        val usecasesDummy = JLabel(Translations.getString("application.notYetImplemented.text"))
        usecasesDummy.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        usecasesDummy.verticalAlignment = JLabel.TOP

        rightSidebarPane.add(Translations.getString("graph.scenarios.title"), "/img/scenarios-16.png", scenarioPanel)
        rightSidebarPane.add(Translations.getString("graph.usecases.title"), "/img/usecase-16.png", usecasesDummy)

        bottomSidebarPane.add(Translations.getString("graph.issues.title"), "/img/issue-16.png", issuesPanel)

        mainSplitPane.add(librarySplitPane)
        mainSplitPane.add(graphNavigationPanel)
        mainSplitPane.dividerLocation = BaseModule.settings.getInt("graphPanel.mainSplitPos", 250)
        mainSplitPane.border = null

        mainPanel.add(mainSplitPane, BorderLayout.CENTER)
        mainPanel.add(rightSidebarPane, BorderLayout.EAST)

        add(mainPanel, BorderLayout.CENTER)
        add(bottomSidebarPane, BorderLayout.SOUTH)
    }

    /** Handles changes of the ´isOpen´ property of the [rightSidebarPane]. */
    private fun rightSidebarPaneChanged() {
        if (rightSidebarPane.isOpen) {
            mainPanel.removeAll()
            rightSidebarSplitPane.remove(rightSidebarPane)
            rightSidebarSplitPane.remove(mainSplitPane)
            rightSidebarSplitPane.add(mainSplitPane)
            rightSidebarSplitPane.add(rightSidebarPane)
            rightSidebarSplitPane.dividerLocation = if (rightSidebarDividerLocation > 0) rightSidebarDividerLocation else mainSplitPane.width - DEF_SIDEBAR_SIZE
            rightSidebarDividerLocation = rightSidebarSplitPane.dividerLocation
            mainPanel.add(rightSidebarSplitPane, BorderLayout.CENTER)
        } else {
            rightSidebarDividerLocation = rightSidebarSplitPane.dividerLocation
            mainPanel.removeAll()
            rightSidebarSplitPane.remove(rightSidebarPane)
            rightSidebarSplitPane.remove(mainSplitPane)
            mainPanel.add(mainSplitPane, BorderLayout.CENTER)
            mainPanel.add(rightSidebarPane, BorderLayout.EAST)
        }
        revalidate()
        repaint()
    }

    /** Handles changes of the ´isOpen´ property of the [bottomSidebarPane]. */
    private fun bottomSidebarPaneChanged() {
        if (bottomSidebarPane.isOpen) {
            removeAll()
            bottonSidebarSplitPane.remove(mainPanel)
            bottonSidebarSplitPane.remove(bottomSidebarPane)
            bottonSidebarSplitPane.add(mainPanel)
            bottonSidebarSplitPane.add(bottomSidebarPane)
            bottonSidebarSplitPane.dividerLocation = if(bottomSidebarDividerLocation > 0) bottomSidebarDividerLocation else mainPanel.height - DEF_SIDEBAR_SIZE
            bottomSidebarDividerLocation = bottonSidebarSplitPane.dividerLocation
            add(bottonSidebarSplitPane, BorderLayout.CENTER)
        } else {
            bottomSidebarDividerLocation = bottonSidebarSplitPane.dividerLocation
            removeAll()
            bottonSidebarSplitPane.remove(mainPanel)
            bottonSidebarSplitPane.remove(bottomSidebarPane)
            add(mainPanel, BorderLayout.CENTER)
            add(bottomSidebarPane, BorderLayout.SOUTH)
        }
        revalidate()
        repaint()
    }

    private fun setMode(mode: ApplicationMode, init: Boolean) {
        currentMode = mode

        when(currentMode) {
            ApplicationMode.EDIT -> {
                if (!init) {
                    scheduler.isActive = false
                }
                editor.active = true
                eventBus.post(ApplicationModeEvent(currentMode))
            }
            ApplicationMode.EXECUTE -> {
                editor.view.selectionManager.deselectAll()
                editor.active = false
                InvocationHandler.invoke(Runnable {
                    scheduler.isActive = true
                    eventBus.post(ApplicationModeEvent(currentMode))
                })
            }
        }
    }

    private fun createExecutionToolBar(): JToolBar {
        val modeToggleButton = JToggleButton(modeToggleAction)
        modeToggleButton.text = null
        modeToggleButton.isFocusPainted = false
        modeToggleButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/powerOff-24.png"))
        modeToggleButton.selectedIcon = ImageIcon(GraphPanel::class.java.getResource("/img/powerOff-24.png"))
        modeToggleButton.toolTipText = modeToggleAction.getValue(Action.LONG_DESCRIPTION) as String?
        modeToggleButton.isFocusPainted = false

        val pauseToggleButton = JToggleButton(ActionWrapperSwing(PauseExecutionAction(scheduler, eventBus)))
        pauseToggleButton.text = null
        modeToggleButton.isFocusPainted = false
        pauseToggleButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/PauseOff-24.png"))
        pauseToggleButton.selectedIcon = ImageIcon(GraphPanel::class.java.getResource("/img/PauseOff-24.png"))

        val stepButton = JButton(ActionWrapperSwing(StepExecutionAction(scheduler, eventBus)))
        stepButton.text = null
        stepButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/Resume-24.png"))

        val speedSlider = SystemSpeedSlider()
        speedSlider.maximumSize = Dimension(200, speedSlider.maximumSize.height)

        val mainToolBar = JToolBar()
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
	    toolbar.addTool(PolylineTool(editor, { PolylineComponent() }, { GraphElementViewWrapper<Vertice>(it) }),
		    "/img/polyline.gif", Translations.getString("edit.component.polyline"))
        toolbar.addTool(QuadCurveTool(editor, { QuadCurveComponent() }, { GraphElementViewWrapper<Vertice>(it)}),
                "/img/curve24.png", Translations.getString("edit.component.quadraticCurve"))

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
class EditedGraphViewEvent(val oldGraphView: GraphView<GraphElementView<*>>, val newGraphView: GraphView<GraphElementView<*>>)