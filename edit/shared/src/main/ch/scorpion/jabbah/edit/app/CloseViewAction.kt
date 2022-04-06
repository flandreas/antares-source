package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ContentViewManager

/** Asks the [View] currently active in [ContentViewManager] to close.*/
class CloseViewAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractViewAction("view.action.close", eventBus, viewManager) {

	init {
		updateEnabled()
	}

	override fun execute(event: ActionEvent) {
		viewManager.activeView?.let {
			eventBus.post(CloseViewRequest(it))
		}
	}
}