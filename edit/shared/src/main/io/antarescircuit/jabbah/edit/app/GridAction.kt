package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Grid

/**
 * An action for toggling the visibility of the currently active [View]'s [Grid].
 */
class GridAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractEditAction("view.action.grid", eventBus, viewManager) {

	init {
		updateState()
	}

	override fun handleViewPropertyChanged(e: PropertyChangeEvent<Any>) {
		super.handleViewPropertyChanged(e)
		updateState()
	}

	override fun execute(event: io.antarescircuit.jabbah.base.event.ActionEvent) {
		val view = viewManager.activeView?.view
		if (view is DrawingView<*,*>) {
			view.showGrid = !view.showGrid
		}
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && (drawingView?.editable ?: false)
	}

	private fun updateState() {
		if (viewManager.activeView?.view is DrawingView<*,*>) {
			selected = (viewManager.activeView!!.view as DrawingView<*,*>).showGrid
		}
	}
}