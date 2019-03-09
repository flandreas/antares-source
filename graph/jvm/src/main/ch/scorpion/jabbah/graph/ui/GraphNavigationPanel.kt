package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerRunningStateEvent
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.ScenarioEvent
import ch.scorpion.jabbah.graph.view.scenario.ScenarioDetector
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.io.StorableCreator
import java.awt.*
import javax.swing.*
import javax.swing.border.Border


/**
 * Displays a [GraphView] in a [DrawingView] along with a [NavigationStackView] that allows the user
 * to navigate within the [GraphView] hierarchy.
 */
open class GraphNavigationPanel(
    private val isRoot: Boolean,
    val drawingView: DrawingView<GraphView<GraphElementView<*>>>,
    private val viewManager: ViewManager,
    private val closeHandler: ((GraphNavigationPanel) -> Unit)?,
    contextBorderColor: CompositeColor? = null,
    private val scheduler: Scheduler,
    private val animator: Animator,
    private val eventBus: EventBus,
    private val repository: MetaGraphRepository,
    private val storableCreator: StorableCreator,
    private val scriptGateway: ScriptGateway,
    private val currentSystemSpeedCategory: CurrentSystemSpeedCategory
) : JPanel() {

    companion object {
        private val LOG by logger(GraphNavigationPanel::class)

	    /** The name of the [Boolean] property in [Properties] that controls whether animations are used when opening subgraphs.*/
	    const val PROP_DIVE_ANIMATION = "graph.GraphNavigationPanel.diveAnimation"
    }

    private val mainPanel = JPanel(BorderLayout())

    private val headerPanel = JPanel()

    private val navigationStackView = NavigationStackView()

    private val layeredPane = JLayeredPane()

    private var scenarioDetector: ScenarioDetector? = null

    private var currentMode: ApplicationMode = if (scheduler.isActive) ApplicationMode.EXECUTE else ApplicationMode.EDIT

    private val openSubGraphRequestHandler: (OpenSubGraphRequest) -> Unit = { handle(it) }
    private val navigationStackEventHandler: (NavigationStackEvent) -> Unit = { handle(it) }
    private val applicationModeEventHandler: (ApplicationModeEvent) -> Unit = { handle(it) }
    private val scenarioEventHandler: (ScenarioEvent) -> Unit = { handle(it) }
    private val schedulerActivationStateHandler: (SchedulerActivationStateEvent) -> Unit = { handle(it) }
	private val schedulerRunningStateHandler: (SchedulerRunningStateEvent) -> Unit = { handle(it) }
    private val systemSpeedHandler: (SystemSpeedEvent) -> Unit = { handle(it) }
	private val currentSavableHandler: (CurrentSavableEvent) -> Unit = { handle(it) }
	private val closeViewRequestHandler: (CloseViewRequest) -> Unit = { handle(it) }

    /** Forwards input events to the [GraphView] while executing.*/
    private val graphViewExecutionHandler = GraphViewExecutionHandler(drawingView, scheduler, eventBus)

    /** Forwards input events to the [GraphView] while displaying (i.e. NOT executing) and NOT being editable.*/
    private val graphViewDisplayHandler = GraphViewDisplayHandler(drawingView, scheduler, eventBus)

	/** Forwards input events to the [GraphView] while a [Usecase] is executed.*/
	private val graphViewUsecaseExecutionHandler = GraphViewUsecaseExecutionHandler(drawingView, scheduler, eventBus)

	private var currentSavable: Savable? = null

    var contextColor: CompositeColor? = null
        set(value) {
            if (field == value) {
                return
            }
            when {
                field == null -> {
                    // Add context color border
                    mainPanel.removeAll()
                    mainPanel.add(headerPanel, BorderLayout.NORTH)
                    val borderPanel = JPanel(BorderLayout())
                    borderPanel.border = createContextColorBorder(value!!)
                    borderPanel.add(layeredPane)
                    mainPanel.add(FocusPanel(borderPanel, drawingView, viewManager), BorderLayout.CENTER)
                }
                value == null -> {
                    // Remove context color border
                    mainPanel.removeAll()
                    mainPanel.add(headerPanel, BorderLayout.NORTH)
                    mainPanel.add(FocusPanel(layeredPane, drawingView, viewManager))
                }
                else -> // Exchange context color border
                    (mainPanel.getComponent(0) as JComponent).border = createContextColorBorder(value)
            }
            revalidate()
            repaint()
        }


    init {
        eventBus.register(OpenSubGraphRequest::class, openSubGraphRequestHandler)
        eventBus.register(NavigationStackEvent::class, navigationStackEventHandler)
        eventBus.register(ApplicationModeEvent::class, applicationModeEventHandler)
        eventBus.register(ScenarioEvent::class, scenarioEventHandler)
        eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
	    eventBus.register(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
        eventBus.register(SystemSpeedEvent::class, systemSpeedHandler)
	    eventBus.register(CurrentSavableEvent::class, currentSavableHandler)
	    eventBus.register(CloseViewRequest::class, closeViewRequestHandler)

        drawingView.editable = drawingView.editable && isRoot

        setRootGraphView(drawingView.drawing)

        buildUI(contextBorderColor, closeHandler)
        propagateApplicationContext()

        updateDetached()
    }

    open fun dispose() {
        drawingView.content.drawing.dispose()
        graphViewExecutionHandler.dispose()
        graphViewDisplayHandler.dispose()
	    graphViewUsecaseExecutionHandler.dispose()

	    scenarioDetector?.dispose()

        eventBus.unregister(OpenSubGraphRequest::class, openSubGraphRequestHandler)
        eventBus.unregister(NavigationStackEvent::class, navigationStackEventHandler)
        eventBus.unregister(ApplicationModeEvent::class, applicationModeEventHandler)
        eventBus.unregister(ScenarioEvent::class, scenarioEventHandler)
        eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
	    eventBus.unregister(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
        eventBus.unregister(SystemSpeedEvent::class, systemSpeedHandler)
	    eventBus.unregister(CurrentSavableEvent::class, currentSavableHandler)
	    eventBus.unregister(CloseViewRequest::class, closeViewRequestHandler)
    }

    /** Initializes the [NavigationStackView] with a root [DrawingViewContent].*/
    fun setRootGraphView(graphView: GraphView<GraphElementView<*>>) {
        drawingView.drawing = graphView
        navigationStackView.navigationStack.rootEntry = NavigationStackEntry(content = drawingView.content)
        scenarioDetector?.dispose()
        scenarioDetector = ScenarioDetector(drawingView, scheduler, scriptGateway, eventBus, currentSystemSpeedCategory)
	    UiUtil.invokeLater(Runnable { drawingView.navigator.fitMaxNormal() })

    }

    private fun getRootEntry(): NavigationStackEntry<GraphView<GraphElementView<*>>> =
	    navigationStackView.navigationStack.rootEntry!!

    @Suppress("unused")
    fun setGlassPaneComponent(component: JComponent) {
        removeGlassPaneComponent()
        layeredPane.add(component, JLayeredPane.DRAG_LAYER)
        revalidate()
        repaint()
    }

    @Suppress("MemberVisibilityCanBePrivate")
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

	/** Deselects all [Component]s in all [View]s.*/
	fun deselectAll() {
		drawingView.selectionManager.deselectAll()
		navigationStackView.navigationStack.forAllContents { it.removeAllSelectionModels() }
	}

    /** Finds the first [NavigationStackEntry] in the navigation stack that fulfills the specified condition, if any.*/
    fun findEntry(condition: (NavigationStackEntry<GraphView<GraphElementView<*>>>) -> Boolean): NavigationStackEntry<GraphView<GraphElementView<*>>>? =
            navigationStackView.navigationStack.find(condition)

    private fun handle(request: CloseViewRequest) {
	    if (request.view === drawingView) {
		    closeHandler?.invoke(this)
	    }
    }

	private fun handle(request: OpenSubGraphRequest) {
        LOG.debug("handling OpenSubGraphRequest by descending into SubGraphVerticeView")

        val graphView = drawingView.drawing
        if (!graphView.contains(request.subGraphVerticeView)) {
            return
        }

        if (!request.newView) {
	        val subGraphView = request.subGraphVerticeView.createSubGraphView()
	        navigationStackView.navigationStack.peek().content.zoomPan = drawingView.zoomPan
	        if (BaseModule.properties.getBoolean(PROP_DIVE_ANIMATION) && !request.quickMode) {
		        drawingView.userZoomEnabled = false
		        navigationStackView.isEnabled = false
		        DescendAnimationManager(animator).descendInto(
			        drawingView,
			        request.subGraphVerticeView,
			        descender = {
				        navigationStackView.navigationStack.push(NavigationStackEntry(
					        subGraphVerticeView = request.subGraphVerticeView,
					        content = drawingView.createContent(subGraphView as GraphView<GraphElementView<*>>)))
			        },
			        terminator = {
				        navigationStackView.isEnabled = true
				        drawingView.userZoomEnabled = true
			        }
		        )
	        } else {
		        SwingUtilities.invokeLater {
			        navigationStackView.navigationStack.push(NavigationStackEntry(
				        subGraphVerticeView = request.subGraphVerticeView,
				        content = drawingView.createContent(subGraphView as GraphView<GraphElementView<*>>)))
		        }
	        }
        }
    }

    private fun handle(event: NavigationStackEvent) {
        if (event.navigationStack !== navigationStackView.navigationStack) {
            return
        }
        if (navigationStackView.navigationStack.peek().content === drawingView.content) {
            return
        }

	    if (!event.isExpansion && !event.quickMode && BaseModule.properties.getBoolean(PROP_DIVE_ANIMATION) && !event.quickMode) {
		    ascendFrom(event.entries)
	    } else {
		    drawingView.content = navigationStackView.navigationStack.peek().content
		    updateDrawingViewEditability()
		    updateDetached()

		    invalidate()
		    revalidate()
	    }
    }

	private fun ascendFrom(entries: List<NavigationStackEntry<*>>) {
		drawingView.userZoomEnabled = false
		navigationStackView.isEnabled = false
		DescendAnimationManager(animator).ascendFrom(
			drawingView = drawingView,
			subGraphVerticeView = entries.last().subGraphVerticeView!!,
			endZoomFactor = 1.0,
			ascender = {
				drawingView.content = if (entries.size > 1) {
					entries[entries.size - 2].content as DrawingViewContent<GraphView<GraphElementView<*>>>
				} else {
					navigationStackView.navigationStack.peek().content
				}
			},
			terminator = if (entries.size == 1) {
				{
					updateDrawingViewEditability()
					updateDetached()

					navigationStackView.isEnabled = true
					drawingView.userZoomEnabled = true
				}
			} else {
				{
					ascendFrom(entries.subList(0, entries.size - 1))
				}
			}
		)
	}

    private fun handle(event: ApplicationModeEvent) {
        currentMode = event.applicationMode
        propagateApplicationContext()
	    updateDrawingViewEditability()
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
                getRootEntry().content.drawing.graph!!.bind(repository, storableCreator)
            }
            navigationStackView.forEach { it.content.drawing.bind() }
            getRootEntry().content.drawing.graph!!.executionStarted(event.scheduler)
        } else {
            getRootEntry().content.drawing.graph!!.executionStopped(event.scheduler)
        }
    }

	private fun handle(event: SchedulerRunningStateEvent) {
		propagateApplicationContext()
	}

    private fun handle(@Suppress("UNUSED_PARAMETER") event: SystemSpeedEvent) {
        propagateApplicationContext()
    }

	private fun handle(event: CurrentSavableEvent) {
		currentSavable = event.savable
		updateDrawingViewEditability()
	}

    private fun updateDrawingViewEditability() {
	    drawingView.editable = isRoot && navigationStackView.navigationStack.size == 1 && !scheduler.isActive && !(currentSavable?.readOnly ?: false)
    }

    private fun propagateApplicationContext() {
        drawingView.applicationContext = GraphApplicationContext(currentMode, currentSystemSpeedCategory, scheduler.isPaused)
    }

    /**
     * Updates the [DrawingView] in order to display whether the displayed [GraphView] is detached,
     * i.e. whether it doesn't show accurate signal states due to shallow execution.
     */
    private fun updateDetached() {
	    drawingView.overlayColor = if ((!isRoot || navigationStackView.navigationStack.size > 1)
            && scheduler.isActive
            && !scheduler.isDeepExecution
            && StringUtils.isNotEmpty(drawingView.drawing.graph!!.script)
        ) {
		    Themes.get<GraphTheme>().overlay
        } else {
            null
        }
    }

    private fun createContextColorBorder(contextColor: CompositeColor): Border =
            BorderFactory.createLineBorder(Graphics2DJvm.toAwtColor(contextColor.backgroundColor), 5, true)

    private fun buildUI(contextColor: CompositeColor?, closeHandler: ((GraphNavigationPanel) -> Unit)?) {
        layeredPane.layout = LayerLayoutManager()
        layeredPane.add(drawingView.canvas as JComponent, JLayeredPane.DEFAULT_LAYER)

        headerPanel.layout = BoxLayout(headerPanel, BoxLayout.LINE_AXIS)
        headerPanel.add(navigationStackView)
        if (closeHandler != null) {
            val closeButton = JButton("Close")
            closeButton.addActionListener { closeHandler.invoke(this@GraphNavigationPanel) }
	        closeButton.icon = ImageIcon(GraphNavigationPanel::class.java.getResource("/img/close-16.png"))
            closeButton.text = null
            closeButton.border = BorderFactory.createEmptyBorder(0, 0, 0, 10)
            headerPanel.add(Box.createHorizontalGlue())
            headerPanel.add(closeButton)
        }
	    headerPanel.background =  Graphics2DJvm.toAwtColor(DrawModule.properties.getColor(NavigationStackView.PROP_PANEL_BACKGROUND_COLOR))

        mainPanel.add(headerPanel, BorderLayout.NORTH)

        mainPanel.add(FocusPanel(layeredPane, drawingView, viewManager))
        this.contextColor = contextColor

        layout = BorderLayout()
        add(mainPanel, BorderLayout.CENTER)
    }

    /** Layouts all contained [Component]s to fully take up the entire size of the parent [Container].*/
    private inner class LayerLayoutManager : LayoutManager {

        override fun layoutContainer(parent: Container?) {
            synchronized(parent!!.treeLock) {
                if (parent.componentCount > 0) {
                    for (i in 0 until parent.componentCount) {
                        parent.getComponent(i).setBounds(0, 0, parent.width, parent.height)
                    }
                }
            }
        }

        override fun preferredLayoutSize(parent: Container?): Dimension =
                (drawingView.canvas as JComponent).preferredSize

        override fun minimumLayoutSize(parent: Container?): Dimension = Dimension()

        override fun addLayoutComponent(name: String?, comp: Component?) {
            // empty
        }

        override fun removeLayoutComponent(comp: Component?) {
            // empty
        }
    }
}