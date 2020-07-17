package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.animation.AnimationModule
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
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomStrategy
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.view.ComponentMessageDisplayer
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerRunningStateEvent
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.ScenarioEvent
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.scenario.ScenarioDetector
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import javax.swing.JComponent
import javax.swing.JLayeredPane
import javax.swing.JPanel
import javax.swing.SwingUtilities


/**
 * Displays a [GraphView] in a [DrawingView] along with a [NavigationStackViewSwing] that allows the user
 * to navigate within the [GraphView] hierarchy.
 */
class GraphNavigationPanel(
	private val isRoot: Boolean,
	override val drawingView: DrawingView<GraphView>,
	private val viewManager: ViewManager,
	contextBorderColor: CompositeColor? = null,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	private val animator: Animator = AnimationModule.animator,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val scriptGateway: ScriptGateway = ScriptModule.scriptGateway,
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	extensionFactory: (GraphNavigationPanel) -> GraphNavigationPanelExtension = GraphModuleJvm.graphNavigationPanelExtensionFactory
) : AbstractGraphDesktopItemPanel() {

	companion object {
		private val LOG by logger(GraphNavigationPanel::class)

		/** The name of the [Boolean] property in [Properties] that controls whether animations are used when opening subgraphs.*/
		const val PROP_DIVE_ANIMATION = "graph.GraphNavigationPanel.diveAnimation"
	}

	private val mainPanel = JPanel(BorderLayout())

	private val navigationStackViewController = NavigationStackViewController()

	private val navigationStackView = NavigationStackViewSwing(navigationStackViewController)

	private val headerPanel = GraphDesktopItemHeaderPanel(this, navigationStackView, eventBus, allowClose = true)

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
	private val graphViewExecutionHandler = GraphViewExecutionHandler(drawingView, scheduler, eventBus, currentMode)

	/** Forwards input events to the [GraphView] while displaying (i.e. NOT executing) and NOT being editable.*/
	private val graphViewDisplayHandler = GraphViewDisplayHandler(drawingView, scheduler, eventBus)

	/** Forwards input events to the [GraphView] while a [Usecase] is executed.*/
	private val graphViewUsecaseExecutionHandler = GraphViewUsecaseExecutionHandler(drawingView, scheduler, eventBus, currentMode)

	private var currentSavable: Savable? = null

	private val extension = extensionFactory.invoke(this)

	private val globalMessageDisplayer: ComponentMessageDisplayer<Drawing<Component>>? =
		if (isRoot) ComponentMessageDisplayer(drawingView as DrawingView<Drawing<Component>>, true, eventBus, animator) else null

	val showsNavigationRoot: Boolean get() = navigationStackViewController.navigationStack.size == 1

	init {
		navigationStackViewController.view = navigationStackView

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

		buildUI(contextBorderColor)
		propagateApplicationContext()

		updateDetached()
	}

	/** ---- [GraphDesktopItem] */

	override fun dispose() {
		drawingView.dispose()
		navigationStackViewController.dispose()
		graphViewExecutionHandler.dispose()
		graphViewDisplayHandler.dispose()
		graphViewUsecaseExecutionHandler.dispose()

		scenarioDetector?.dispose()
		globalMessageDisplayer?.dispose()

		eventBus.unregister(OpenSubGraphRequest::class, openSubGraphRequestHandler)
		eventBus.unregister(NavigationStackEvent::class, navigationStackEventHandler)
		eventBus.unregister(ApplicationModeEvent::class, applicationModeEventHandler)
		eventBus.unregister(ScenarioEvent::class, scenarioEventHandler)
		eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.unregister(SchedulerRunningStateEvent::class, schedulerRunningStateHandler)
		eventBus.unregister(SystemSpeedEvent::class, systemSpeedHandler)
		eventBus.unregister(CurrentSavableEvent::class, currentSavableHandler)
		eventBus.unregister(CloseViewRequest::class, closeViewRequestHandler)

		extension.dispose(this)
	}

	override fun addContextColorBorder(color: Color) {
		mainPanel.removeAll()
		mainPanel.add(headerPanel, BorderLayout.NORTH)
		val borderPanel = JPanel(BorderLayout())
		borderPanel.border = createContextColorBorder(color)
		borderPanel.add(layeredPane)
		mainPanel.add(FocusPanel(borderPanel, drawingView, viewManager), BorderLayout.CENTER)
	}

	override fun removeContextColorBorder() {
		mainPanel.removeAll()
		mainPanel.add(headerPanel, BorderLayout.NORTH)
		mainPanel.add(FocusPanel(layeredPane, drawingView, viewManager))
	}

	/** ---- [GraphNavigationPanel] */

	/** Initializes the [NavigationStackViewSwing] with a root [DrawingViewContent].*/
	fun setRootGraphView(graphView: GraphView, applyZoomStrategy: Boolean = true) {
		val oldZoomStrategy = drawingView.defaultZoomStrategy
		if (!applyZoomStrategy) {
			drawingView.defaultZoomStrategy = ZoomStrategy.NONE
		}
		drawingView.drawing = graphView
		if (!applyZoomStrategy) {
			drawingView.defaultZoomStrategy = oldZoomStrategy
		}

		navigationStackViewController.navigationStack.rootEntry = NavigationStackEntry(content = drawingView.content)
		scenarioDetector?.dispose()
		scenarioDetector = ScenarioDetector(drawingView, scheduler, scriptGateway, eventBus, currentSystemSpeedCategory)
	}

	private fun getRootEntry(): NavigationStackEntry<GraphView> =
		navigationStackViewController.navigationStack.rootEntry!!

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
		navigationStackViewController.navigationStack.rootEntry?.content?.selectionManager?.deselectAll()
		navigationStackViewController.navigationStack.forAllContents { it.removeAllSelectionModels() }
	}

	/** Finds the first [DrawingViewContent] in the navigation stack that fulfills the specified condition, if any.*/
	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? =
		navigationStackViewController.navigationStack.find(condition)


	private fun handle(request: CloseViewRequest) {
		if (request.view === drawingView) {
			eventBus.post(GraphDesktopItemCloseRequest(this))
		}
	}

	private fun handle(request: OpenSubGraphRequest) {
		if (!shouldDescendFor(request)) {
			return
		}
		LOG.debug("handling OpenSubGraphRequest by descending into SubGraphVerticeView")

		rememberZoomPanOfCurrentNavigationStack()
		if (isDescendAnimationRequired(request)) {
			descendIntoSubGraphWithAnimation(request.subGraphVerticeView)
		} else {
			descendIntoSubGraphWithoutAnimation(request.subGraphVerticeView)
		}
	}

	private fun shouldDescendFor(request: OpenSubGraphRequest): Boolean {
		return !request.newView && drawingView.drawing.contains(request.subGraphVerticeView)
	}

	private fun isDescendAnimationRequired(request: OpenSubGraphRequest): Boolean {
		return BaseModule.properties.getBoolean(PROP_DIVE_ANIMATION) && !request.quickMode
	}

	private fun rememberZoomPanOfCurrentNavigationStack() {
		navigationStackViewController.navigationStack.peek().content.zoomPan = drawingView.zoomPan
	}

	private fun descendIntoSubGraphWithAnimation(vv: SubGraphVerticeView<*>) {
		drawingView.userZoomEnabled = false
		navigationStackView.isEnabled = false
		DescendAnimationManager(animator).descendInto(
			drawingView,
			vv,
			descender = {
				navigationStackViewController.navigationStack.push(NavigationStackEntry(
					subGraphVerticeView = vv,
					content = drawingView.createContent(vv.createSubGraphView())))
			},
			terminator = {
				navigationStackView.isEnabled = true
				drawingView.userZoomEnabled = true
			}
		)
	}

	private fun descendIntoSubGraphWithoutAnimation(vv: SubGraphVerticeView<*>) {
		SwingUtilities.invokeLater {
			navigationStackViewController.navigationStack.push(NavigationStackEntry(
				subGraphVerticeView = vv,
				content = drawingView.createContent(vv.createSubGraphView())))
			UiUtil.invokeLater(Runnable { drawingView.navigator.fitMaxNormal() })
		}
	}

	private fun handle(event: NavigationStackEvent) {
		if (event.navigationStack !== navigationStackViewController.navigationStack) {
			return
		}
		if (navigationStackViewController.navigationStack.peek().content === drawingView.content) {
			return
		}

		if (!event.isExpansion && !event.quickMode && BaseModule.properties.getBoolean(PROP_DIVE_ANIMATION) && !event.quickMode) {
			ascendFrom(event.entries)
		} else {
			drawingView.content = navigationStackViewController.navigationStack.peek().content
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
					entries[entries.size - 2].content as DrawingViewContent<GraphView>
				} else {
					navigationStackViewController.navigationStack.peek().content
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

	private fun handle(@Suppress("UNUSED_PARAMETER") event: SchedulerRunningStateEvent) {
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
		drawingView.editable = isRoot && navigationStackViewController.navigationStack.size == 1 && !scheduler.isActive && !(currentSavable?.readOnly
			?: false)
	}

	private fun propagateApplicationContext() {
		drawingView.applicationContext = GraphApplicationContext(currentMode, currentSystemSpeedCategory, scheduler.isPaused)
	}

	/**
	 * Updates the [DrawingView] in order to display whether the displayed [GraphView] is detached,
	 * i.e. whether it doesn't show accurate signal states due to shallow execution.
	 */
	private fun updateDetached() {
		drawingView.overlayColor = if ((!isRoot || navigationStackViewController.navigationStack.size > 1)
			&& scheduler.isActive
			&& !scheduler.isDeepExecution
			&& StringUtils.isNotEmpty(drawingView.drawing.graph!!.script)
		) {
			Themes.get<GraphTheme>().overlay
		} else {
			null
		}
	}

	private fun buildUI(contextColor: CompositeColor?) {
		layeredPane.layout = LayerLayoutManager()
		layeredPane.add(drawingView.canvas as JComponent, JLayeredPane.DEFAULT_LAYER)

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

		override fun addLayoutComponent(name: String?, comp: java.awt.Component?) {
			// empty
		}

		override fun removeLayoutComponent(comp: java.awt.Component?) {
			// empty
		}
	}
}