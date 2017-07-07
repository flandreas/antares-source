package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.ScenarioEvent
import ch.scorpion.jabbah.graph.view.scenario.ScenarioDetector
import ch.scorpion.jabbah.graph.view.ui.GraphViewDisplayHandler
import ch.scorpion.jabbah.graph.view.ui.DescendAnimationManager
import ch.scorpion.jabbah.graph.view.ui.GraphViewExecutionHandler
import ch.scorpion.jabbah.graph.view.ui.NavigationStackEvent
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.base.logger
import java.awt.*
import javax.swing.*


/**
 * Displays a [GraphView] in a [DrawingView] along with a [NavigationStackView] that allows the user
 * to navigate within the [GraphView] hierarchy.
 */
open class GraphNavigationPanel(
        val isRoot: Boolean,
        val drawingView: DrawingView<GraphView<GraphElementView<*>>>,
        viewManager: ViewManager,
        closeHandler: ((GraphNavigationPanel) -> Unit)?,
        private val scheduler: Scheduler,
        private val animator: Animator,
        private val eventBus: EventBus,
        private val libraryHolder: LibraryHolder,
        private val storableCreator: StorableCreator,
        private val scriptGateway: ScriptGateway
) : JPanel() {

    private val LOG by logger()

    private val navigationStackView = NavigationStackView()

    private val layeredPane = JLayeredPane()

    private val focusPanel = FocusPanel(layeredPane, drawingView, viewManager)

    private var scenarioDetector: ScenarioDetector? = null

    private val openSubGraphRequestHandler: (OpenSubGraphRequest) -> Unit = {handle(it)}
    private val navigationStackEventHandler: (NavigationStackEvent) -> Unit = {handle(it)}
    private val applicationModeEventHandler: (ApplicationModeEvent) -> Unit = {handle(it)}
    private val scenarioEventHandler: (ScenarioEvent) -> Unit = {handle(it)}
    private val schedulerActivationStateHandler: (SchedulerActivationStateEvent) -> Unit = {handle(it)}

    init {

        GraphViewExecutionHandler(drawingView, scheduler, eventBus)
        GraphViewDisplayHandler(drawingView, scheduler, eventBus)

        eventBus.register(OpenSubGraphRequest::class, openSubGraphRequestHandler)
        eventBus.register(NavigationStackEvent::class, navigationStackEventHandler)
        eventBus.register(ApplicationModeEvent::class, applicationModeEventHandler)
        eventBus.register(ScenarioEvent::class, scenarioEventHandler)
        eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)

        drawingView.showGrid = isRoot

        setRootGraphView(drawingView.drawing)

        buildUI(closeHandler)
    }

    open fun dispose() {
        drawingView.drawing.dispose()
        eventBus.unregister(OpenSubGraphRequest::class, openSubGraphRequestHandler)
        eventBus.unregister(NavigationStackEvent::class, navigationStackEventHandler)
        eventBus.unregister(ApplicationModeEvent::class, applicationModeEventHandler)
        eventBus.unregister(ScenarioEvent::class, scenarioEventHandler)
        eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
    }

    fun setRootGraphView(graphView: GraphView<GraphElementView<*>>) {
        navigationStackView.navigationStack.rootGraphView = graphView
        scenarioDetector?.dispose()
        scenarioDetector = ScenarioDetector(drawingView, scheduler, scriptGateway, eventBus)
    }

    private fun getRootGraphView(): GraphView<GraphElementView<*>> {
        return navigationStackView.navigationStack.rootGraphView!!
    }

    @Suppress("unused")
    fun setGlassPaneComponent(component: JComponent) {
        removeGlassPaneComponent()
        layeredPane.add(component, JLayeredPane.DRAG_LAYER)
        revalidate()
        repaint()
    }

    fun removeGlassPaneComponent() {
        val components = layeredPane.getComponentsInLayer(JLayeredPane.DRAG_LAYER)
        if (components != null) {
            for (i in components.indices) {
                layeredPane.remove(components[i])
            }
            revalidate()
            repaint()
        }
    }

    private fun handle(request: OpenSubGraphRequest) {
        LOG.debug("handling OpenSubGraphRequest by diving into SubGraphVerticeView")

        val graphView = drawingView.drawing
        if (!graphView.contains(request.subGraphVerticeView)) {
            return
        }

        val subGraphView = request.subGraphVerticeView.createSubGraphView()

        if (!request.quickMode) {
            DescendAnimationManager(animator).descendInto(
                    drawingView,
                    request.subGraphVerticeView,
                    diver = {
                        navigationStackView.isEnabled = false
                        navigationStackView.navigationStack.push(subGraphView as GraphView<GraphElementView<*>>)
                    },
                    ender = { navigationStackView.isEnabled = true})
        }
    }

    private fun handle(event: NavigationStackEvent) {
        if (event.navigationStack !== navigationStackView.navigationStack) {
            return
        }
        val graphView = navigationStackView.navigationStack.peek()
        setGraphView(graphView)

        invalidate()
        revalidate()
    }

    private fun handle(event: ApplicationModeEvent) {
        drawingView.applicationContext = event.applicationMode
        updateDetached()
    }

    private fun handle(event: ScenarioEvent) {
        if (event.graphView === drawingView.drawing) {
            drawingView.drawing.invalidate()
            drawingView.drawing.validate()
        }
    }

    private fun handle(event: SchedulerActivationStateEvent) {
        if (event.scheduler.isActive) {
            if (isRoot) {
                getRootGraphView().graph!!.bind(libraryHolder.library, storableCreator)
            }
            navigationStackView.forEach { it.bind() }
            getRootGraphView().graph!!.executionStarted(event.scheduler)
        } else {
            getRootGraphView().graph!!.executionStopped(event.scheduler)
        }
    }

    private fun setGraphView(graphView: GraphView<GraphElementView<*>>) {
        drawingView.drawing = graphView
        drawingView.editable = navigationStackView.navigationStack.size == 1
        updateDetached()
    }

    /**
     * Updates the [DrawingView] in order to display whether the displayed [GraphView] is detached,
     * i.e. whether it doesn't show accurate signal states due to shallow execution.
     */
    private fun updateDetached() {
        if (
                (!isRoot || navigationStackView.navigationStack.size > 1)
                && scheduler.isActive
                && !scheduler.isDeepExecution
                && StringUtils.isNotEmpty(drawingView.drawing.graph!!.script)
        ) {
            // TODO Make color configurable after feature has passed experimental stage
            drawingView.overlayColor = Color(255, 255, 255, 192)
        } else {
            drawingView.overlayColor = null
        }
    }

    private fun buildUI(closeHandler: ((GraphNavigationPanel) -> Unit)?) {
        layeredPane.layout = LayerLayoutManager()
        layeredPane.add(drawingView.canvas as JComponent, JLayeredPane.DEFAULT_LAYER)

        val headerPanel = JPanel()
        headerPanel.layout = BoxLayout(headerPanel, BoxLayout.LINE_AXIS)
        headerPanel.add(navigationStackView)
        if (closeHandler != null) {
            val closeButton = JButton("Close")
            closeButton.addActionListener({ closeHandler.invoke(this@GraphNavigationPanel) })
            closeButton.icon = ImageIcon(GraphNavigationPanel::class.java.getResource("/img/close-16.png"))
            closeButton.text = null
            closeButton.border = BorderFactory.createEmptyBorder(0, 0, 0, 10)
            headerPanel.add(Box.createHorizontalGlue())
            headerPanel.add(closeButton)
        }

        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(headerPanel, BorderLayout.NORTH)
        mainPanel.add(focusPanel, BorderLayout.CENTER)

        layout = BorderLayout()
        add(mainPanel, BorderLayout.CENTER)
    }

    /** Layouts all contained [Component]s to fully take up the entire size of the parent [Container].*/
    private inner class LayerLayoutManager : LayoutManager {

        override fun layoutContainer(parent: Container?) {
            synchronized(parent!!.treeLock) {
                if (parent.componentCount > 0) {
                    for (i in 0..parent.componentCount - 1) {
                        parent.getComponent(i).setBounds(0, 0, parent.width, parent.height)
                    }
                }
            }
        }

        override fun preferredLayoutSize(parent: Container?): Dimension {
            return (drawingView.canvas as JComponent).preferredSize
        }

        override fun minimumLayoutSize(parent: Container?): Dimension {
           return Dimension()
        }

        override fun addLayoutComponent(name: String?, comp: Component?) {
            // empty
        }

        override fun removeLayoutComponent(comp: Component?) {
            // empty
        }
    }
}