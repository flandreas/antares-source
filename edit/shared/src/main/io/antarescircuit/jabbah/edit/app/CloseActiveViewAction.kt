package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.CloseViewRequest
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.view.AbstractContentViewAction
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule

/** Asks the [View] currently active in [ContentViewManager] to close.*/
class CloseActiveViewAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractContentViewAction("view.action.close", eventBus, viewManager) {

	init {
		updateEnabled()
	}

	override fun execute(event: ActionEvent) {
		viewManager.activeView?.let {
			eventBus.post(CloseViewRequest(it))
		}
	}
}