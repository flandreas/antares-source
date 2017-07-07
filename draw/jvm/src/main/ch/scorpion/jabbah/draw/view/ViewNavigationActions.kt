package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import java.awt.event.ActionEvent
import java.lang.Float

/**
 * A base implementation of an [Action] that acts on the currently active [View] in a [ViewManager]
 * and that disables itself if no [View] is active.
 */
abstract class AbstractViewAction(
        baseName: String,
        eventBus: EventBus,
        val viewManager: ViewManager
) : AbstractAction(baseName) {

    companion object {
        /** The name of the zoom step [Float] in [Properties]. */
        val PROP_ZOOM_STEP = "view.command.zoom.step"
    }

    init {
        eventBus.register(ActiveViewChangedEvent::class, {
            activeViewChanged(it.oldView, it.newView)
        })
    }

    protected open fun activeViewChanged(oldView: View<out InputEventContext>?, newView: View<out InputEventContext>?) {
        isEnabled = viewManager.activeView != null
    }
}

/** An action for zooming the currently active [View] to normal size and panning to the center.*/
class ZoomNormalAction(viewManager: ViewManager, eventBus: EventBus)
    : AbstractViewAction("view.action.zoomNormal", eventBus, viewManager) {

    override fun actionPerformed(e: ActionEvent?) {
        viewManager.activeView!!.navigator.panCenter(1.0)
    }
}

/** An action for zooming into the currently active [View] .*/
class ZoomInAction(viewManager: ViewManager, eventBus: EventBus)
    : AbstractViewAction("view.action.zoomIn", eventBus, viewManager) {

    override fun actionPerformed(e: ActionEvent?) {
        val view = viewManager.activeView!!
        view.navigator.setZoomFactor(view.zoomFactor * BaseModule.properties.getFloat(PROP_ZOOM_STEP))
    }
}

/** An action for zooming out from the currently active [View] .*/
class ZoomOutAction(viewManager: ViewManager, eventBus: EventBus)
    : AbstractViewAction("view.action.zoomOut", eventBus, viewManager) {

    override fun actionPerformed(e: ActionEvent?) {
        val view = viewManager.activeView!!
        view.navigator.setZoomFactor(view.zoomFactor / BaseModule.properties.getFloat(PROP_ZOOM_STEP))
    }
}

/**
 * An action for zooming and panning the currently active [View] such that the [Drawing] fills the entire
 * available view space.
 */
class ZoomFitAction(viewManager: ViewManager, eventBus: EventBus)
    : AbstractViewAction("view.action.zoomFit", eventBus, viewManager) {

    override fun actionPerformed(e: ActionEvent?) {
        viewManager.activeView!!.navigator.fit()
    }
}

/** An action for centering the currently active [View] without changing the zoom factor.*/
class ZoomCenterAction(viewManager: ViewManager, eventBus: EventBus)
    : AbstractViewAction("view.action.zoomCenter", eventBus, viewManager) {

    override fun actionPerformed(e: ActionEvent?) {
        val view = viewManager.activeView!!
        view.navigator.panCenter(view.zoomFactor)
    }
}