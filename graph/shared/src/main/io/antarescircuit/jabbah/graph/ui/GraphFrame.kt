package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ApplicationDataEvent
import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.app.BeforeSaveEvent
import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.ZoomStrategy
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolderImpl
import io.antarescircuit.jabbah.graph.container.isManualContainer
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementSavable
import io.antarescircuit.jabbah.graph.library.CurrentLibraryEvent
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.LibraryServiceCallbackAdapter
import io.antarescircuit.jabbah.graph.model.graph.GraphPropagationDelayCalculator
import io.antarescircuit.jabbah.graph.ui.container.ContainerPanelController
import io.antarescircuit.jabbah.graph.ui.container.ContainerPanelView
import io.antarescircuit.jabbah.graph.ui.documentation.DocumentationPanelController
import io.antarescircuit.jabbah.graph.ui.graphpanel.GraphPanelView
import io.antarescircuit.jabbah.graph.ui.graphpanel.GraphPanelViewController
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

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

		const val CONTAINER_EDITOR_NAME = "containerEditor"
	}

	var displayedView: DisplayedView = DisplayedView.Desktop
		private set

	private val systemSpeed = SystemSpeed(eventBus = eventBus)

	private val systemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed)

	private val scheduler = SchedulerImpl(systemSpeedCategory)

	/** Spawns a individual [GraphApplicationContextHolder] with its separate [Scheduler] instance.*/
	val applicationContextHolder = GraphApplicationContextHolder(scheduler, systemSpeed = systemSpeed, currentSystemSpeedCategory = systemSpeedCategory)

	private val drawingView = EditModule.drawingViewFactory.create(
		GraphViewModule.graphViewFactory.create(null),
		applicationContextHolder,
		displayGlobalMessages = true,
		name = MAIN_EDITOR_NAME)

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
		drawingView,
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

	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = { handle(it) }

	/**
	 * Stops possible active execution before saving so that [Actor.executionStopped] gets called
	 * allowing them to reset non-persistent state they could have changed during execution.
	 * */
	private val beforeSaveHandler: EventHandler<BeforeSaveEvent> = { handle(it) }

	override fun onViewInitialized() {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
		eventBus.register(BeforeSaveEvent::class, beforeSaveHandler)

		registerZoomEventHandlers()
		LibraryModule.libraryServiceCallbacks.add(customSymbolHandler)
		if (BaseModule.properties.getBoolean(GraphPropagationDelayCalculator.PROP_CALCULATE_ON_SAVE)) {
			LibraryModule.libraryServiceCallbacks.add(propagationDelayCalculator)
		}
		showDesktop()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
		eventBus.unregister(currentLibraryHandler)
		eventBus.unregister(beforeSaveHandler)

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

	private fun handle(@Suppress("unused") event: CurrentLibraryEvent) {
		System.invokeLater {
			(viewContainerAction as GraphFrameController<*>.AbstractViewAction).update()
			(viewDocumentationAction as GraphFrameController<*>.AbstractViewAction).update()
			showDesktop()
		}
	}

	private fun handle(@Suppress("unused") event: BeforeSaveEvent) {
		scheduler.isActive = false
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

		abstract fun update()
	}

	private inner class ViewDesktopAction(
		eventBus: EventBus
	) : AbstractViewAction("graph.action.showDesktop", eventBus) {

		init {
			imagePath = "/img/drawing-24.png"
		}

		override fun execute(event: ActionEvent) {
			LOG.userTrail("View desktop")
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
			LOG.userTrail("View container")
			showContainer()
		}

		override fun update() {
			selected = displayedView == DisplayedView.Container
			enabled = view.applicationMode.isEdit()
				&& appDataViewController.data?.savable is AbstractContainerLibraryElementSavable
		}
	}

	private inner class ViewDocumentationAction(
		eventBus: EventBus
	) : AbstractViewAction("graph.action.showDocumentation", eventBus) {

		init {
			imagePath = "/img/documentation.png"
		}

		override fun execute(event: ActionEvent) {
			LOG.userTrail("View documentation")
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
				if (!metaGraph.isManualContainer) {
					LOG.userTrail("Container (Symbol) has been customized manually")
					metaGraph.isManualContainer = true
				}
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