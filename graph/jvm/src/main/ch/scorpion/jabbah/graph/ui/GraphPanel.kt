package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ActiveViewChangedEvent
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.PropertySheetPanelFactory
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineTool
import ch.scorpion.jabbah.edit.model.rectangle.RectangleTool
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.model.text.SimpleTextComponent
import ch.scorpion.jabbah.edit.model.text.TextTool
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.execution.PauseExecutionAction
import ch.scorpion.jabbah.execution.StepExecutionAction
import ch.scorpion.jabbah.execution.SystemSpeedSlider
import ch.scorpion.jabbah.execution.module.ExecutionModule
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
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioPanel
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.graph.view.GraphTextComponent
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*


/**
 * A [JPanel] for editing and executing a root [GraphView]. It consists of a [LibraryPanel] at the left,
 * a [ComponentPropertyPanel] for editing the properties of the selected [Component] at the bottom left,
 * and a [GraphNavigationPanel] for editing the [GraphView] at the right.
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

    constructor(editor: Editor, viewManager: ViewManager): this(
        editor,
        BaseModule.eventBus,
        LibraryModule.libraryHolder,
        viewManager,
        GraphModuleJvm.graphNavigationPanelFactory,
        ExecutionModule.scheduler,
        EditModuleJvm.propertySheetPanelFactory)

    private val modeToggleAction = ToggleModeAction(scheduler, eventBus)

    val libraryPanel = LibraryPanel(eventBus, libraryHolder)

    val scenarioPanel = ScenarioPanel(editor, eventBus, propertySheetFactory)

    val libraryPropertyPanel: ComponentPropertyPanel

    val graphNavigationPanel = graphNavigationPanelFactory.create(
        true,
        editor.view as DrawingView<GraphView<GraphElementView<*>>>,
        viewManager,
        null,
        scheduler)

    private val drawingToolBar = createDrawingToolBar()

    private val settingsToolBar = createSettingsToolBar()

    val toolbars: List<JToolBar> = listOf(
            createExecutionToolBar(),
            drawingToolBar,
            settingsToolBar)

    private val librarySplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

    private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

    private var currentMode: ApplicationMode = ApplicationMode.EDIT

    init {
        (editor.view.canvas as JComponent).transferHandler = createTransferHandler(editor, eventBus)
        libraryPropertyPanel = ComponentPropertyPanel(editor, propertySheetFactory, eventBus)

        eventBus.register(ApplicationDataEvent::class, { handleApplicationDataChanged(it) })
        eventBus.register(ActiveViewChangedEvent::class, {
            updateEditability()
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
        BaseModule.properties.set("graphPanel.mainSplitPos", mainSplitPane.dividerLocation)
        BaseModule.properties.set("graphPanel.librarySplitPos", librarySplitPane.dividerLocation)
    }

    private fun createTransferHandler(editor: Editor, eventBus: EventBus): TransferHandler {
        return GraphPanelTransferHandler(editor, eventBus, GraphElementViewTransferable.FLAVOR, libraryHolder)
    }

    private fun handleApplicationDataChanged(event: ApplicationDataEvent) {
        val metaGraph = event.data as MetaGraph
        val graphView = metaGraph.graph!!.graphView as GraphView<GraphElementView<*>>
        graphNavigationPanel.setRootGraphView(graphView)
        scenarioPanel.graphView = graphView

        eventBus.post(EditedGraphViewEvent(graphView))
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
        librarySplitPane.dividerLocation = BaseModule.properties.getInt("graphPanel.librarySplitPos", 700)

        val toolTabbedPane = JTabbedPane()
        toolTabbedPane.border = null
        toolTabbedPane.background = Color(238, 238, 238)
        toolTabbedPane.addTab(Translations.getString("graph.library.title"), librarySplitPane)
        toolTabbedPane.addTab(Translations.getString("graph.scenarios.title"), scenarioPanel)

        mainSplitPane.add(toolTabbedPane)
        mainSplitPane.add(graphNavigationPanel)
        mainSplitPane.dividerLocation = BaseModule.properties.getInt("graphPanel.mainSplitPos", 250)

        add(mainSplitPane, BorderLayout.CENTER)
    }

    private fun setMode(mode: ApplicationMode, init: Boolean) {
        currentMode = mode
        editor.view.applicationContext = mode

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

        val pauseToggleButton = JToggleButton(PauseExecutionAction(scheduler, eventBus))
        pauseToggleButton.text = null
        modeToggleButton.isFocusPainted = false
        pauseToggleButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/PauseOff-24.png"))
        pauseToggleButton.selectedIcon = ImageIcon(GraphPanel::class.java.getResource("/img/PauseOff-24.png"))

        val stepButton = JButton(StepExecutionAction(scheduler, eventBus))
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
        // TODO I18N
        toolbar.addTool(editor.currentTool, "/img/pointer.gif", "Selektion")
        toolbar.addTool(RectangleTool(editor, { RectangularComponent() }, { GraphElementViewWrapper<Vertice>(it) }), "/img/rectangle.gif", "Selektion")
        toolbar.addTool(PolylineTool(editor, { PolylineComponent() }, { GraphElementViewWrapper<Vertice>(it) }), "/img/polyline.gif", "Polylinie")
        // TEST BEGIN
        // toolbar.addTool(TextTool(editor, { GraphTextComponent() }, { GraphElementViewWrapper<Vertice>(it)}), "/img/text.gif", "Text")
        toolbar.addTool(TextTool(editor, { SimpleTextComponent("This is a text") }, { GraphElementViewWrapper<Vertice>(it)}), "/img/text.gif", "Text")
        // TEST END

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
        button.toolTipText = "Snap"

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
                    updateState()
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
class EditedGraphViewEvent(val graphView: GraphView<*>)