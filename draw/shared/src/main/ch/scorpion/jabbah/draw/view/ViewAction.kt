package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View

/**
 * A base implementation of an [Action] that acts on the currently active [View] in a [ViewManager]
 * and that disables itself if no [View] is active.
 */
abstract class AbstractViewAction(
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus,
	val viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractAction(baseName) {

	companion object {
		/** The name of the zoom step [Float] in [Properties]. */
		const val PROP_ZOOM_STEP = "view.command.zoom.step"
	}

	init {
		eventBus.register(ActiveViewChangedEvent::class) { activeViewChanged(it.oldView, it.newView) }
	}

	private val viewPropertyListener = object : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (View.PROP_USER_ZOOM_ENABLED == e.name) {
				updateEnabled()
			}
		}
	}

	protected open fun activeViewChanged(oldView: View<out InputEventContext>?, newView: View<out InputEventContext>?) {
		oldView?.removePropertyChangeListener(viewPropertyListener)
		updateEnabled()
		newView?.addPropertyChangeListener(viewPropertyListener)
	}

	protected fun updateEnabled() {
		enabled = calculateEnabled()
	}

	protected open fun calculateEnabled(): Boolean {
		return viewManager.activeView != null && viewManager.activeView!!.userZoomEnabled
	}
}

/** An action for zooming the currently active [View] to normal size and panning to the center.*/
class ZoomNormalAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractViewAction("view.action.zoomNormal", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		viewManager.activeView!!.navigator.panCenter(1.0)
	}
}

/** An action for zooming into the currently active [View] .*/
class ZoomInAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractViewAction("view.action.zoomIn", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView!!
		view.navigator.multiplyZoomFactor(BaseModule.properties.getFloat(PROP_ZOOM_STEP).toDouble())
	}
}

/** An action for zooming out from the currently active [View] .*/
class ZoomOutAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractViewAction("view.action.zoomOut", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView!!
		view.navigator.multiplyZoomFactor(1 / BaseModule.properties.getFloat(AbstractViewAction.PROP_ZOOM_STEP).toDouble())
	}
}

/**
 * An action for zooming and panning the currently active [View] such that the content fills the entire
 * available view space.
 */
class ZoomFitAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractViewAction("view.action.zoomFit", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		viewManager.activeView!!.navigator.fit()
	}
}

/** An action for centering the currently active [View] without changing the zoom factor.*/
class ZoomCenterAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractViewAction("view.action.zoomCenter", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView!!
		view.navigator.panCenter(view.zoomFactor)
	}
}
