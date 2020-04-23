package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Grid

/**
 * An action for toggling the visibility of the currently active [View]'s [Grid].
 */
class GridAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractEditAction("view.action.grid", eventBus, viewManager) {

	init {
		updateState()
	}

	override fun handleViewPropertyChanged(e: PropertyChangeEvent<Any>) {
		super.handleViewPropertyChanged(e)
		updateState()
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView
		if (view is DrawingView<*>) {
			view.showGrid = !view.showGrid
		}
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && (drawingView?.editable ?: false)
	}

	private fun updateState() {
		if (viewManager.activeView is DrawingView<*>) {
			selected = (viewManager.activeView as DrawingView<*>).showGrid
		}
	}
}