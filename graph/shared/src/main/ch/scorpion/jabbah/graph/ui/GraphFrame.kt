package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomStrategy
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.container.isManualContainer
import ch.scorpion.jabbah.graph.ui.documentation.DocumentationPanelController
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementSavable
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryServiceCallbackAdapter
import ch.scorpion.jabbah.graph.model.graph.GraphPropagationDelayCalculator
import ch.scorpion.jabbah.graph.ui.container.ContainerPanelController
import ch.scorpion.jabbah.graph.ui.container.ContainerPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewController
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * The main UI element of a graph [Application] that allows to switch between
 * a [GraphPanelView] (for editing the inside view of the main [GraphView])
 * and a [ContainerPanelView] (for editing the outside view of the main [GraphView]).
 */
interface GraphFrame : UIView {

	val applicationMode: ApplicationMode

	val desktopView: View<*>

	val containerView: View<*>

	val desktopViewShowsNavigationRoot: Boolean

	/** Called by [GraphFrameController] when [GraphFrameController.displayedView] has changed.*/
	fun notifyDisplayedView()
}

/** Posted by [GraphFrameController] on [EventBus] when [GraphFrameController.displayedView] has changed.*/
data class GraphFrameEvent(
	val source: GraphFrameController<*>,
	val displayedView: GraphFrameController.DisplayedView
)

/** Defines the part of the controller that is used by the view.*/
interface GraphFrameActions {
	val viewDesktopAction: Action
	val viewContainerAction: Action
	val viewDocumentationAction: Action
}

open class GraphFrameController<T: GraphFrame>(
	private val appDataViewController: ApplicationDataViewController,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val properties: Properties = BaseModule.properties
) : AbstractUIController<T>(), GraphFrameActions {

	enum class DisplayedView {
		Desktop,
		Container,
		Documentation
	}

	companion object {

		private val LOG by logger(GraphFrameController::class)

		/** The name of the [Boolean] property in [Properties] that controls whether extreme zoom factors should initiate mode switching.*/
		const val PROP_AUTO_SWITCH = "graph.GraphFrame.autoSwitch"

		/** The percentage of the minimal zoom factor that switches this [GraphFrame] to display the container.*/
		const val SWITCH_TO_CONTAINER_ZOOM_FACTOR_PERCENTAGE = 1.1

		/** The percentage of the maximal zoom factor that switches this [GraphFrame] to display the desktop.*/
		const val SWITCH_TO_DESKTOP_ZOOM_FACTOR_PERCENTAGE = 0.9

		/** The name of the tag set in [CommandManager] when [GraphFrameController.DisplayedView.Container] is displayed.*/
		const val EDIT_CONTAINER_TAG = "editContainer"

		const val GENERATE_CONTAINER_TAG = "generateContainer"

		const val MAIN_EDITOR_NAME = "mainEditor"
	}

	var displayedView: DisplayedView = DisplayedView.Desktop
		private set

	private val systemSpeed = SystemSpeed(eventBus = eventBus)

	private val systemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed)

	private val scheduler = SchedulerImpl(systemSpeedCategory)

	/** Spawns a individual [GraphApplicationContextHolder] with its separate [Scheduler] instance.*/
	val applicationContextHolder = GraphApplicationContextHolder(scheduler, systemSpeed = systemSpeed, currentSystemSpeedCategory = systemSpeedCategory)

	private val drawingView = EditModule.drawingViewFactory.create(
		GraphViewModule.graphViewFactory.create(null) as Drawing<Component>,
		applicationContextHolder,
		displayGlobalMessages = true
	)

	val editor: Editor = GraphViewModule.graphEditorFactory.invoke(MAIN_EDITOR_NAME, drawingView)

	val applicationModeHolder = ApplicationModeHolderImpl(editor, scheduler).also {
		// Cyclic dependency
		applicationContextHolder.applicationModeHolder = it
	}

	override val viewDesktopAction: Action = ViewDesktopAction(eventBus)
	override val viewContainerAction: Action = ViewContainerAction(eventBus)
	override val viewDocumentationAction: Action = ViewDocumentationAction(eventBus)

	val containerPanelController = ContainerPanelController(applicationContextHolder, displayGlobalMessages = true, drawingView)

	val graphPanelViewController = GraphPanelViewController(
		editor,
		appDataViewController,
		applicationContextHolder,
		applicationModeHolder,
		eventBus)

	val documentationPanelController = DocumentationPanelController(appDataViewController, eventBus)

	private val zoomEventHandler = ZoomEventHandler()

	private val customSymbolHandler = CustomSymbolHandler()

	private val propagationDelayCalculator = PropagationDelayCalculator()

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = { handle(it) }

	override fun onViewInitialized() {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		registerZoomEventHandlers()
		LibraryModule.libraryServiceCallbacks.add(customSymbolHandler)
		LibraryModule.libraryServiceCallbacks.add(propagationDelayCalculator)
		showDesktop()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
		viewDesktopAction.dispose()
		viewContainerAction.dispose()
		viewDocumentationAction.dispose()
		graphPanelViewController.dispose()
		documentationPanelController.dispose()
		applicationContextHolder.dispose()
		unregisterZoomEventHandlers()
		LibraryModule.libraryServiceCallbacks.remove(customSymbolHandler)
		LibraryModule.libraryServiceCallbacks.remove(propagationDelayCalculator)
	}

	private fun showDesktop() {
		// Unconditional due to initialization
		displayedView = DisplayedView.Desktop
		view.notifyDisplayedView()
		editor.commandManager.removeTag(EDIT_CONTAINER_TAG)
		eventBus.post(GraphFrameEvent(this, displayedView))
	}

	private fun showContainer() {
		if (displayedView != DisplayedView.Container) {
			displayedView = DisplayedView.Container
			view.notifyDisplayedView()
			editor.commandManager.addTag(EDIT_CONTAINER_TAG)
			eventBus.post(GraphFrameEvent(this, displayedView))
		}
	}

	private fun showDocumentation() {
		if (displayedView != DisplayedView.Documentation) {
			displayedView = DisplayedView.Documentation
			view.notifyDisplayedView()
			editor.commandManager.removeTag(EDIT_CONTAINER_TAG)
			eventBus.post(GraphFrameEvent(this, displayedView))
		}
	}

	private fun registerZoomEventHandlers() {
		view.desktopView.addPropertyChangeListener(zoomEventHandler)
		view.containerView.addPropertyChangeListener(zoomEventHandler)
	}

	private fun unregisterZoomEventHandlers() {
		view.desktopView.removePropertyChangeListener(zoomEventHandler)
		view.containerView.removePropertyChangeListener(zoomEventHandler)
	}

	private fun handle(event: ApplicationModeEvent) {
		if (event.source === applicationModeHolder) {
			appDataViewController.isSavable = applicationModeHolder.currentMode == ApplicationMode.EDIT
		}
	}

	private inner class ZoomEventHandler: PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == View.PROP_TRANSFORMATION && properties.getBoolean(PROP_AUTO_SWITCH) && applicationModeHolder.currentMode == ApplicationMode.EDIT) {
				// Switch mode only if the ZoomPan change was intentionally initiated by the user
				if (view.desktopView.zoomStrategy == ZoomStrategy.NONE) {
					if (e.source === view.desktopView && view.desktopViewShowsNavigationRoot) {
						if (view.desktopView.zoomFactor <= SWITCH_TO_CONTAINER_ZOOM_FACTOR_PERCENTAGE * properties.getFloat(
								View.PROP_MIN_ZOOM_FACTOR
							)
						) {
							showContainer()
							System.invokeLater {
								view.containerView.canvas.requestViewFocus()
							}
						}
					} else if (e.source === view.containerView) {
						if (view.containerView.zoomFactor >= SWITCH_TO_DESKTOP_ZOOM_FACTOR_PERCENTAGE * properties.getFloat(
								View.PROP_MAX_ZOOM_FACTOR
							)
						) {
							showDesktop()
							System.invokeLater {
								view.desktopView.canvas.requestViewFocus()
							}
						}
					}
				}
			}
		}
	}

	private abstract inner class AbstractViewAction(
		baseName: String,
		private val eventBus: EventBus
	) : AbstractAction(baseName) {

		private val graphFrameHandler: EventHandler<GraphFrameEvent> = { update() }
		private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { update() }

		private val applicationModeHandler: EventHandler<ApplicationModeEvent> = {
			if (it.source === applicationModeHolder) {
				update()
			}
		}

		init {
			eventBus.register(GraphFrameEvent::class, graphFrameHandler)
			eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
			eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		}

		override fun dispose() {
			super.dispose()
			eventBus.unregister(graphFrameHandler)
			eventBus.unregister(applicationDataHandler)
		}

		protected abstract fun update()
	}

	private inner class ViewDesktopAction(
		eventBus: EventBus
	) : AbstractViewAction("graph.action.showDesktop", eventBus) {

		init {
			imagePath = "/img/drawing-24.png"
		}

		override fun execute(event: ActionEvent) {
			showDesktop()
		}

		override fun update() {
			selected = displayedView == DisplayedView.Desktop
			enabled = view.applicationMode.isEdit()
		}
	}

	private inner class ViewContainerAction(
		eventBus: EventBus
	) : AbstractViewAction("graph.action.showContainer", eventBus) {

		init {
			imagePath = "/img/container-24.png"
		}

		override fun execute(event: ActionEvent) {
			showContainer()
		}

		override fun update() {
			selected = displayedView == DisplayedView.Container
			enabled = view.applicationMode.isEdit() && appDataViewController.data?.savable is AbstractContainerLibraryElementSavable
		}
	}

	private inner class ViewDocumentationAction(
		eventBus: EventBus
	) : AbstractViewAction("graph.action.showDocumentation", eventBus) {

		init {
			imagePath = "/img/documentation.png"
		}

		override fun execute(event: ActionEvent) {
			showDocumentation()
		}

		override fun update() {
			selected = displayedView == DisplayedView.Documentation
			enabled = view.applicationMode.isEdit() && appDataViewController.data?.savable is AbstractContainerLibraryElementSavable
		}
	}

	private inner class CustomSymbolHandler : LibraryServiceCallbackAdapter() {
		override fun beforeStoreMetaGraph(metaGraph: MetaGraph) {
			if (isManualContainer(metaGraph.isManualContainer, editor.commandManager)) {
				LOG.userTrail("Container (Symbol) has been customized manually")
				metaGraph.isManualContainer = true
			}
		}
	}

	private inner class PropagationDelayCalculator : LibraryServiceCallbackAdapter() {
		override fun beforeStoreMetaGraph(metaGraph: MetaGraph) {
			val model = metaGraph.graph.model ?: return
			if (model.overallPropagationDelay == null) {
				val delay = GraphPropagationDelayCalculator().calculate(model)
				model.calculatedPropagationDelay = if (delay >= 0) {
					LOG.debug("Calculated Graph propagation delay to $delay ns")
					delay
				} else {
					null
				}
			}
		}
	}
}