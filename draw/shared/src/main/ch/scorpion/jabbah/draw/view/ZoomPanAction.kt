package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View

/**
 * A base implementation of a [AbstractViewAction] that lets the user change zoom or pan.
 * It is only enabled if [View.userZoomEnabled] is `true`.
 */
abstract class AbstractZoomPanAction(
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractViewAction(baseName, eventBus, viewManager) {

	companion object {
		/** The name of the zoom step [Float] in [Properties]. */
		const val PROP_ZOOM_STEP = "view.command.zoom.step"
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && viewManager.activeView!!.userZoomEnabled
	}

	override fun handleViewPropertyChanged(e: PropertyChangeEvent<Any>) {
		super.handleViewPropertyChanged(e)
		if (View.PROP_USER_ZOOM_ENABLED == e.name) {
			updateEnabled()
		}
	}
}

/** An action for zooming the currently active [View] to normal size and panning to the center.*/
class ZoomNormalAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction("view.action.zoomNormal", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		viewManager.activeView!!.navigator.panCenterDefault()
	}
}

/** An action for zooming into the currently active [View] .*/
class ZoomInAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction("view.action.zoomIn", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView!!
		view.navigator.multiplyZoomFactor(BaseModule.properties.getFloat(PROP_ZOOM_STEP).toDouble())
	}
}

/** An action for zooming out from the currently active [View] .*/
class ZoomOutAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction("view.action.zoomOut", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView!!
		view.navigator.multiplyZoomFactor(1 / BaseModule.properties.getFloat(PROP_ZOOM_STEP).toDouble())
	}
}

/**
 * An action for zooming and panning the currently active [View] such that the content fills the entire
 * available view space.
 */
class ZoomFitAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction("view.action.zoomFit", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		viewManager.activeView!!.navigator.fit()
	}
}

/** An action for centering the currently active [View] without changing the zoom factor.*/
class ZoomCenterAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction("view.action.zoomCenter", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView!!
		view.navigator.panCenter(view.zoomFactor)
	}
}