package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomStrategy

/**
 * A base implementation of a [AbstractViewAction] that lets the user change zoom or pan.
 * It is only enabled if [View.userZoomEnabled] is `true`.
 */
abstract class AbstractZoomPanAction(
	private val zoomStrategy: ZoomStrategy,
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractViewAction(baseName, eventBus, viewManager) {

	companion object {
		/** The name of the zoom step [Float] in [Properties]. */
		const val PROP_ZOOM_STEP = "view.command.zoom.step"
	}

	override fun execute(event: ActionEvent) {
		viewManager.activeView!!.view!!.zoomStrategy = zoomStrategy
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && viewManager.activeView?.view?.userZoomEnabled == true

	override fun handleViewPropertyChanged(e: PropertyChangeEvent<Any>) {
		super.handleViewPropertyChanged(e)
		when (e.name) {
			View.PROP_USER_ZOOM_ENABLED -> updateEnabled()
			View.PROP_ZOOM_STRATEGY -> updateSelected()
		}
	}

	override fun notifyActiveViewChanged() {
		updateSelected()
	}

	protected open fun calculateSelected(): Boolean = viewManager.activeView?.view?.zoomStrategy == zoomStrategy

	private fun updateSelected() {
		selected = calculateSelected()
	}
}

/** An action for zooming the currently active [View] to normal size and panning to the center.*/
class ZoomNormalAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.NORMAL,"view.action.zoomNormal", eventBus, viewManager)

/** An action for zooming into the currently active [View] .*/
class ZoomInAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.NONE, "view.action.zoomIn", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val view = viewManager.activeView!!.view!!
		view.navigator.multiplyZoomFactor(BaseModule.properties.getFloat(PROP_ZOOM_STEP).toDouble())
		view.zoomStrategy = ZoomStrategy.NONE
	}
}

/** An action for zooming out from the currently active [View] .*/
class ZoomOutAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.NONE,"view.action.zoomOut", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val view = viewManager.activeView!!.view!!
		view.navigator.multiplyZoomFactor(1 / BaseModule.properties.getFloat(PROP_ZOOM_STEP).toDouble())
		view.zoomStrategy = ZoomStrategy.NONE
	}
}

/**
 * An action for zooming and panning the currently active [View] such that the content fills the entire
 * available view space.
 */
class ZoomFitAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.FIT, "view.action.zoomFit", eventBus, viewManager)

/**
 * An action for zooming and panning the currently active [View] such that the content fills the entire
 * available view space, but not larger than normal zoom.
 */
class ZoomFitMaxNormalAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.FIT_MAX_NORMAL, "view.action.zoomFitMaxNormal", eventBus, viewManager)

/** An action for centering the currently active [View] without changing the zoom factor.*/
class ZoomCenterAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.CENTER, "view.action.zoomCenter", eventBus, viewManager)