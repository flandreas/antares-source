package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewController
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * The main UI element of a graph [Application] that allows to switch between
 * a [GraphPanelView] (for editing the inside view of the main [GraphView] and
 * a ContainerPanel (for editing the outside view of the main [GraphView]).
 */
interface GraphFrame : UIView {

	enum class DisplayedView {
		Desktop,
		Container
	}

	val displayedView: DisplayedView

	val applicationMode: ApplicationMode

	val desktopView: View<*>

	val containerView: View<*>

	val desktopViewShowsNavigationRoot: Boolean

	fun showDesktop()

	fun showContainer()
}

/** Posted by [GraphFrame] on [EventBus] when [GraphFrame.DisplayedView] has changed.*/
data class GraphFrameEvent(
	val graphFrame: GraphFrame,
	val displayedView: GraphFrame.DisplayedView
)

/** Defines the part of the controller that is used by the view.*/
interface GraphFrameActions {
	val viewDesktopAction: Action
	val viewContainerAction: Action
}

open class GraphFrameController<T: GraphFrame>(
	applicationDataHolder: ApplicationDataHolder,
	eventBus: EventBus = BaseModule.eventBus,
	editor: Editor = GraphViewModule.graphEditorFactory.invoke(eventBus),
	viewManager: ViewManager = DrawViewModule.viewManager,
	scheduler: Scheduler = ExecutionModule.scheduler,
	private val properties: Properties = BaseModule.properties
) : AbstractUIController<T>(), GraphFrameActions {

	companion object {

		/** The name of the [Boolean] property in [Properties] that controls whether extreme zoom factors should initiate mode switching.*/
		const val PROP_AUTO_SWITCH = "graph.GraphFrame.autoSwitch"

		/** The percentage of the minimal zoom factor that switches this [GraphFrame] to display the container.*/
		const val SWITCH_TO_CONTAINER_ZOOM_FACTOR_PERCENTAGE = 1.1

		/** The percentage of the maximal zoom factor that switches this [GraphFrame] to display the desktop.*/
		const val SWITCH_TO_DESKTOP_ZOOM_FACTOR_PERCENTAGE = 0.9
	}

	override val viewDesktopAction: Action = ViewDesktopAction(eventBus)
	override val viewContainerAction: Action = ViewContainerAction(eventBus)

	val graphPanelViewController = GraphPanelViewController(editor, applicationDataHolder, viewManager, scheduler, eventBus)

	private val zoomEventHandler = ZoomEventHandler()

	override fun onViewInitialized() {
		registerZoomEventHandlers()
	}

	override fun dispose() {
		super.dispose()
		unregisterZoomEventHandlers()
	}

	private fun registerZoomEventHandlers() {
		view.desktopView.addPropertyChangeListener(zoomEventHandler)
		view.containerView.addPropertyChangeListener(zoomEventHandler)
	}

	private fun unregisterZoomEventHandlers() {
		view.desktopView.removePropertyChangeListener(zoomEventHandler)
		view.containerView.removePropertyChangeListener(zoomEventHandler)
	}

	private inner class ZoomEventHandler: PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == View.PROP_ZOOM_PAN && properties.getBoolean(PROP_AUTO_SWITCH)) {
				if (e.source === view.desktopView && view.desktopViewShowsNavigationRoot) {
					if (view.desktopView.zoomFactor <= SWITCH_TO_CONTAINER_ZOOM_FACTOR_PERCENTAGE * properties.getFloat(View.PROP_MIN_ZOOM_FACTOR)) {
						view.showContainer()
					}
				} else if (e.source === view.containerView) {
					if (view.containerView.zoomFactor >= SWITCH_TO_DESKTOP_ZOOM_FACTOR_PERCENTAGE * properties.getFloat(View.PROP_MAX_ZOOM_FACTOR)) {
						view.showDesktop()
					}
				}
			}
		}
	}

	private inner class ViewDesktopAction(eventBus: EventBus) : AbstractAction("graph.action.showDesktop") {

		init {
			imagePath = "/img/drawing-24.png"
			eventBus.register(GraphFrameEvent::class) { update() }
			eventBus.register(ApplicationModeEvent::class) { update() }
		}

		override fun execute(event: ActionEvent) {
			view.showDesktop()
		}

		private fun update() {
			selected = view.displayedView == GraphFrame.DisplayedView.Desktop
			enabled = view.applicationMode.isEdit()
		}
	}

	private inner class ViewContainerAction(eventBus: EventBus) : AbstractAction("graph.action.showContainer") {

		init {
			imagePath = "/img/container-24.png"
			eventBus.register(GraphFrameEvent::class) { update() }
			eventBus.register(ApplicationModeEvent::class) { update() }
		}

		override fun execute(event: ActionEvent) {
			view.showContainer()
		}

		private fun update() {
			selected = view.displayedView == GraphFrame.DisplayedView.Container
			enabled = view.applicationMode.isEdit()
		}
	}
}