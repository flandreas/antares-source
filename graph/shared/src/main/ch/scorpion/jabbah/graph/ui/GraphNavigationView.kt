package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.ZoomStrategy
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.view.ComponentMessageDisplayer
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewExecutionController
import ch.scorpion.jabbah.graph.view.ScenarioEvent
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.scenario.ScenarioDetector
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Displays a [GraphView] in a [DrawingView] along with a [NavigationStackView] that allows the user
 * to navigate within the [GraphView] hierarchy.
 */
interface GraphNavigationView : UIView {

	fun refresh()
}

/**
 * A controller of a [GraphNavigationView].
 */
class GraphNavigationViewController(
	private val isRoot: Boolean,
	override val drawingView: DrawingView<GraphView>,
	private val isParentDetached: Boolean = false,
	private val animator: Animator = AnimationModule.animator,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val scriptGateway: ScriptGateway = ScriptModule.scriptGateway,
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	extensionFactory: (GraphNavigationViewController) -> GraphNavigationViewControllerExtension = GraphViewModule.graphNavigationViewControllerExtension
) : AbstractUIController<GraphNavigationView>(), GraphViewUI {

	companion object {

		private val LOG by logger(GraphNavigationViewController::class)

		/**
		 * The name of the [Boolean] property in [Properties] that controls whether animations are used
		 * when opening [SubGraphVerticeView]s.
		 */
		const val PROP_DIVE_ANIMATION = "graph.GraphNavigationPanel.diveAnimation"
	}

	val navigationStackViewController = NavigationStackViewController(eventBus = eventBus)
	val navigationStack: NavigationStack<GraphView> get() = navigationStackViewController.navigationStack

	private var currentSavable: Savable? = null
	private var scenarioDetector: ScenarioDetector? = null

	private val openSubGraphRequestHandler: (OpenSubGraphRequest) -> Unit = { handle(it) }
	private val navigationStackEventHandler: (NavigationStackEvent) -> Unit = { handle(it) }
	private val currentSavableHandler: (CurrentSavableEvent) -> Unit = { handle(it) }
	private val scenarioEventHandler: (ScenarioEvent) -> Unit = { handle(it) }
	private val closeViewRequestHandler: (CloseViewRequest) -> Unit = { handle(it) }

	private val rootEntry: NavigationStackEntry<GraphView> get() = navigationStackViewController.navigationStack.rootEntry!!

	private val globalMessageDisplayer: ComponentMessageDisplayer<Drawing<Component>>? =
		if (isRoot) ComponentMessageDisplayer(drawingView as DrawingView<Drawing<Component>>, true, eventBus, animator) else null

	private val extension = extensionFactory.invoke(this)

	private val graphViewExecutionController = GraphViewExecutionController(
		this,
		isRoot,
		rootGraphProvider = { rootEntry.content.drawing.graph!! },
		graphViewsProvider = { navigationStack.iterator().asSequence().map { it.content.drawing }.toList() },
		scheduler,
		eventBus = eventBus
	)

	init {
		eventBus.register(OpenSubGraphRequest::class, openSubGraphRequestHandler)
		eventBus.register(NavigationStackEvent::class, navigationStackEventHandler)
		eventBus.register(CurrentSavableEvent::class, currentSavableHandler)
		eventBus.register(ScenarioEvent::class, scenarioEventHandler)
		eventBus.register(CloseViewRequest::class, closeViewRequestHandler)

		graphViewExecutionController.updateDetachedUI()
	}

	override fun dispose() {
		super.dispose()

		drawingView.dispose()
		navigationStackViewController.dispose()

		scenarioDetector?.dispose()
		globalMessageDisplayer?.dispose()

		graphViewExecutionController.dispose()

		eventBus.unregister(OpenSubGraphRequest::class, openSubGraphRequestHandler)
		eventBus.unregister(NavigationStackEvent::class, navigationStackEventHandler)
		eventBus.unregister(CurrentSavableEvent::class, currentSavableHandler)
		eventBus.unregister(ScenarioEvent::class, scenarioEventHandler)
		eventBus.unregister(CloseViewRequest::class, closeViewRequestHandler)

		extension.dispose(this)
	}

	/** ---- [GraphViewUI] interface */

	/**
	 * Being 'detached' designates that the displayed [GraphView] isn't explicitly simulated
	 * because its graph logic is shadowed by an execution script, or it is a child of a detached parent.
	 */
	override val isDetached: Boolean get() =
		isParentDetached ||
			(!isRoot || navigationStackViewController.navigationStack.size > 1)
			&& StringUtils.isNotEmpty(drawingView.drawing.graph!!.script)

	override val isEditable: Boolean
		get() = navigationStackViewController.navigationStack.size == 1
			&& (currentSavable?.editable ?: false)

	override fun deselectAll() {
		navigationStackViewController.navigationStack.rootEntry?.content?.selectionManager?.deselectAll()
		navigationStackViewController.navigationStack.forAllContents { it.removeAllSelectionModels() }
	}

	/** ---- [GraphNavigationView] */

	fun setRootGraphView(graphView: GraphView, editable: Boolean, applyZoomStrategy: Boolean = true) {
		val oldZoomStrategy = drawingView.defaultZoomStrategy
		if (!applyZoomStrategy) {
			drawingView.defaultZoomStrategy = ZoomStrategy.NONE
		}
		drawingView.drawing = graphView

		drawingView.editable = drawingView.editable && isRoot

		if (!applyZoomStrategy) {
			drawingView.defaultZoomStrategy = oldZoomStrategy
		}

		navigationStackViewController.view.editable = editable
		navigationStackViewController.navigationStack.rootEntry = NavigationStackEntry(content = drawingView.content)

		scenarioDetector?.dispose()
		scenarioDetector = ScenarioDetector(drawingView, scheduler, scriptGateway, eventBus, currentSystemSpeedCategory)

		graphViewExecutionController.updateDetachedUI()
	}

	private fun handle(request: CloseViewRequest) {
		if (request.view === drawingView && view is GraphDesktopViewItem) {
			eventBus.postTwoPhase(
				prepareEvent = GraphDesktopViewItemCloseQuestion(view as GraphDesktopViewItem, isRoot),
				execEvent = GraphDesktopViewItemCloseRequest(view as GraphDesktopViewItem, isRoot)
			)
		}
	}

	private fun handle(event: ScenarioEvent) {
		if (event.graphView === drawingView.drawing) {
			drawingView.drawing.invalidate()
			drawingView.drawing.validate()
		}
	}

	private fun handle(event: CurrentSavableEvent) {
		currentSavable = event.savable
		graphViewExecutionController.updateDrawingViewEditability()
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

	private fun rememberZoomPanOfCurrentNavigationStack() {
		navigationStack.peek().content.zoomPan = drawingView.zoomPan
	}

	private fun isDescendAnimationRequired(request: OpenSubGraphRequest): Boolean {
		return BaseModule.properties.getBoolean(PROP_DIVE_ANIMATION) && !request.quickMode
	}

	private fun descendIntoSubGraphWithAnimation(vv: SubGraphVerticeView<*>) {
		drawingView.userZoomEnabled = false
		navigationStackViewController.view.active = false
		DescendAnimationManager(animator).descendInto(
			drawingView,
			vv,
			descender = {
				navigationStackViewController.navigationStack.push(NavigationStackEntry(
					subGraphVerticeView = vv,
					content = drawingView.createContent(vv.createSubGraphView())))
			},
			terminator = {
				navigationStackViewController.view.active = true
				drawingView.userZoomEnabled = true
			}
		)
	}

	private fun descendIntoSubGraphWithoutAnimation(vv: SubGraphVerticeView<*>) {
		System.invokeLater {
			navigationStackViewController.navigationStack.push(NavigationStackEntry(
				subGraphVerticeView = vv,
				content = drawingView.createContent(vv.createSubGraphView())))
			System.invokeLater {
				drawingView.navigator.fitMaxNormal()
				navigationStackViewController.view.active = true
			}
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
			graphViewExecutionController.updateDrawingViewEditability()
			graphViewExecutionController.updateDetachedUI()

			view.refresh()
		}
	}

	private fun ascendFrom(entries: List<NavigationStackEntry<*>>) {
		drawingView.userZoomEnabled = false
		navigationStackViewController.view.active = false
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
					graphViewExecutionController.updateDrawingViewEditability()
					graphViewExecutionController.updateDetachedUI()

					navigationStackViewController.view.active = true
					drawingView.userZoomEnabled = true
				}
			} else {
				{
					ascendFrom(entries.subList(0, entries.size - 1))
				}
			}
		)
	}
}