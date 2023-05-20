package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ViewTransformation
import ch.scorpion.jabbah.draw.view.ZoomedPointTranslation
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemCloseQuestion
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemCloseRequest
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
interface GraphNavigationView : UIView, GraphDesktopViewItem {

	/** The [GraphView] displayed by this [GraphNavigationView]. */
	val graphView: GraphView

	val showsNavigationRoot: Boolean

	fun refresh()
}

/**
 * A controller of a [GraphNavigationView].
 */
class GraphNavigationViewController(
	val isRoot: Boolean,
	override val drawingView: DrawingView<GraphView>,
	initialSavable: Savable? = null,
	private val isParentDetached: Boolean = false,
	private val animator: Animator = AnimationModule.constantSpeedAnimator,
	private val eventBus: EventBus = BaseModule.eventBus,
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

	/**
	 * The object to be referenced in [CloseViewRequest] sent by this [GraphNavigationView]. Usually  [drawingView], but can
	 * be a different one if this object is wrapped by another object that must be the close target.
	 * */
	lateinit var closeTarget: GraphDesktopViewItem

	val navigationStackViewController = NavigationStackViewController(eventBus = eventBus)
	val navigationStack: NavigationStack<GraphView> get() = navigationStackViewController.navigationStack

	private var editable: Boolean = false

	private var currentSavable: Savable? = initialSavable
	private var scenarioDetector: ScenarioDetector? = null

	private val openSubGraphRequestHandler: (OpenSubGraphRequest) -> Unit = { handle(it) }
	private val navigationStackEventHandler: (NavigationStackEvent) -> Unit = { handle(it) }
	private val currentSavableHandler: (CurrentSavableEvent) -> Unit = { handle(it) }
	private val scenarioEventHandler: (ScenarioEvent) -> Unit = { handle(it) }
	private val closeViewRequestHandler: (CloseViewRequest) -> Unit = { handle(it) }

	private val rootEntry: NavigationStackEntry<GraphView>? get() = navigationStackViewController.navigationStack.rootEntry

	private val extension = extensionFactory.invoke(this)

	private val graphApplicationContextHolder: GraphApplicationContextHolder get() =
		drawingView.applicationContextHolder as GraphApplicationContextHolder

	private val graphViewExecutionController = GraphViewExecutionController(
		this,
		isRoot,
		rootGraphProvider = { rootEntry?.content?.drawing?.graph },
		graphViewsProvider = { navigationStack.iterator().asSequence().map { it.content.drawing }.toList() },
		graphApplicationContextHolder,
		eventBus = eventBus
	)

	private val viewCanvasListener: PropertyChangeListener<Any> = object : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == View.PROP_CANVAS) {
				graphViewExecutionController.updateDetachedUI()
				drawingView.removePropertyChangeListener(this)
			}
		}
	}

	init {
		eventBus.register(OpenSubGraphRequest::class, openSubGraphRequestHandler)
		eventBus.register(NavigationStackEvent::class, navigationStackEventHandler)
		eventBus.register(CurrentSavableEvent::class, currentSavableHandler)
		eventBus.register(ScenarioEvent::class, scenarioEventHandler)
		eventBus.register(CloseViewRequest::class, closeViewRequestHandler)

		drawingView.addPropertyChangeListener(viewCanvasListener)
	}

	override fun onViewInitialized() {
		super.onViewInitialized()
		closeTarget = view
	}

	override fun dispose() {
		super.dispose()

		val graphView = drawingView.drawing

		drawingView.dispose()
		navigationStackViewController.dispose()
		graphView.dispose()

		scenarioDetector?.dispose()

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
		get() = editable
			&& navigationStackViewController.navigationStack.size == 1
			&& (currentSavable?.editable ?: false)

	override fun deselectAll() {
		navigationStackViewController.navigationStack.rootEntry?.content?.selectionManager?.deselectAll()
		navigationStackViewController.navigationStack.forAllContents { it.removeAllSelectionModels() }
	}

	/** ---- [GraphNavigationView] */

	fun setRootGraphView(graphView: GraphView, editable: Boolean, applyZoomStrategy: Boolean = true, originSubGraphVerticeView: SubGraphVerticeView<*>? = null) {
		this.editable = editable

		// Must be done before the drawing is set, because setting the drawing triggers
		// updating the PropertyPanel, whose PropertyEditors get enabled based on DrawingView.editable
		drawingView.editable = editable && isRoot

		drawingView.setDrawing(graphView, applyZoomStrategy)

		navigationStackViewController.view.editable = editable
		navigationStackViewController.navigationStack.rootEntry = NavigationStackEntry(originSubGraphVerticeView, content = drawingView.content)

		scenarioDetector?.dispose()
		scenarioDetector = ScenarioDetector(drawingView, graphApplicationContextHolder, eventBus)

		graphViewExecutionController.updateDetachedUI()
	}

	private fun handle(request: CloseViewRequest) {
		if (request.view === view || request.view === closeTarget) {
			eventBus.postTwoPhase(
				prepareEvent = GraphDesktopViewItemCloseQuestion(closeTarget, isRoot),
				execEvent = GraphDesktopViewItemCloseRequest(closeTarget, isRoot)
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
		LOG.userTrail("Descending into SubGraphVerticeView")

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
		navigationStack.peek().content.transformation =
			ViewTransformation(drawingView.zoomPan, drawingView.transformation.affineTransform.clone())
	}

	private fun isDescendAnimationRequired(request: OpenSubGraphRequest): Boolean {
		return BaseModule.properties.getBoolean(PROP_DIVE_ANIMATION) && !request.quickMode
	}

	private fun rememberVoyageOrigin(vv: SubGraphVerticeView<*>) {
		navigationStackViewController.navigationStack.peek().voyageOrigin = ZoomedPointTranslation(
			vv.boundingBox.center,
			drawingView.modelToView(vv.boundingBox.center),
			drawingView.zoomFactor)
	}

	private fun descendIntoSubGraphWithAnimation(vv: SubGraphVerticeView<*>) {
		drawingView.userZoomEnabled = false
		navigationStackViewController.view.active = false
		rememberVoyageOrigin(vv)
		DescendAnimationManager(animator).descendInto(
			drawingView,
			vv,
			descender = {
				navigationStackViewController.navigationStack.push(NavigationStackEntry(
					subGraphVerticeView = vv,
					content = drawingView.createContent(vv.createSubGraphView(graphApplicationContextHolder.signalHandlerIfActive))))
			},
			terminator = {
				navigationStackViewController.view.active = true
				drawingView.userZoomEnabled = true
			}
		)
	}

	private fun descendIntoSubGraphWithoutAnimation(vv: SubGraphVerticeView<*>) {
		rememberVoyageOrigin(vv)
		System.invokeLater {
			navigationStackViewController.navigationStack.push(NavigationStackEntry(
				subGraphVerticeView = vv,
				content = drawingView.createContent(vv.createSubGraphView(graphApplicationContextHolder.signalHandlerIfActive))))
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
			graphViewExecutionController.updateDrawingViewEditability()

			// This leads to updating the PropertyPanel, which relies on DrawingView.editable
			// and must therefore be done AFTER updating DrawViewEditability in GraphViewExecutionController
			drawingView.content = navigationStackViewController.navigationStack.peek().content

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
			ascender = {
				val outerEntry = if (entries.size > 1) {
					entries[entries.size - 2]
				} else {
					navigationStackViewController.navigationStack.peek()
				}
				drawingView.content = outerEntry.content as DrawingViewContent<GraphView>
				outerEntry.voyageOrigin!!
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