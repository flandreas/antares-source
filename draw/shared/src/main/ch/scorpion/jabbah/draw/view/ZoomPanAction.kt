package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomStrategy
import kotlin.js.JsExport

val zoomInAction: Action by lazy { ZoomInAction() }
val zoomNormalAction: Action by lazy { ZoomNormalAction() }
val zoomOutAction: Action by lazy { ZoomOutAction() }
val zoomFitAction: Action by lazy { ZoomFitAction() }
val zoomFitMaxNormalAction: Action by lazy { ZoomFitMaxNormalAction() }
val zoomCenterAction: Action by lazy { ZoomCenterAction() }

@JsExport
object ZoomPanActions {
	val zoomNormalAction: Action get() = ch.scorpion.jabbah.draw.view.zoomNormalAction
	val zoomInAction: Action get() = ch.scorpion.jabbah.draw.view.zoomInAction
	val zoomOutAction: Action get() = ch.scorpion.jabbah.draw.view.zoomOutAction
	val zoomFitAction: Action get() = ch.scorpion.jabbah.draw.view.zoomFitAction
	val zoomFitMaxNormalAction: Action get() = ch.scorpion.jabbah.draw.view.zoomFitMaxNormalAction
	val zoomCenterAction: Action get() = ch.scorpion.jabbah.draw.view.zoomCenterAction
}

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

	protected val zoomStep: Double = BaseModule.properties.getFloat(PROP_ZOOM_STEP).toDouble()

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
			View.PROP_TRANSFORMATION -> updateEnabled()
		}
	}

	override fun notifyActiveViewChanged() {
		updateSelected()
		updateEnabled()
	}

	protected open fun calculateSelected(): Boolean = viewManager.activeView?.view?.zoomStrategy == zoomStrategy

	private fun updateSelected() {
		selected = calculateSelected()
	}
}

/** An action for zooming the currently active [View] to normal size and panning to the center.*/
private class ZoomNormalAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.NORMAL,"view.action.zoomNormal", eventBus, viewManager)

/** An action for zooming into the currently active [View] .*/
private class ZoomInAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.NONE, "view.action.zoomIn", eventBus, viewManager) {

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled()
			&& view != null
			&& view!!.navigator.isZoomFactorInValidRange(view!!.zoomFactor * zoomStep)

	override fun execute(event: ActionEvent) {
		val view = viewManager.activeView!!.view!!
		view.navigator.multiplyZoomFactor(zoomStep)
		view.zoomStrategy = ZoomStrategy.NONE
	}
}

/** An action for zooming out from the currently active [View] .*/
private class ZoomOutAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.NONE,"view.action.zoomOut", eventBus, viewManager) {

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled()
			&& view != null
			&& view!!.navigator.isZoomFactorInValidRange(1 / view!!.zoomFactor * zoomStep)

	override fun execute(event: ActionEvent) {
		val view = viewManager.activeView!!.view!!
		view.navigator.multiplyZoomFactor(1 / zoomStep)
		view.zoomStrategy = ZoomStrategy.NONE
	}
}

/**
 * An action for zooming and panning the currently active [View] such that the content fills the entire
 * available view space.
 */
private class ZoomFitAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.FIT, "view.action.zoomFit", eventBus, viewManager)

/**
 * An action for zooming and panning the currently active [View] such that the content fills the entire
 * available view space, but not larger than normal zoom.
 */
private class ZoomFitMaxNormalAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.FIT_MAX_NORMAL, "view.action.zoomFitMaxNormal", eventBus, viewManager)

/** An action for centering the currently active [View] without changing the zoom factor.*/
private class ZoomCenterAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractZoomPanAction(ZoomStrategy.CENTER, "view.action.zoomCenter", eventBus, viewManager)