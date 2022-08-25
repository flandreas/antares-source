package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.View

/** Asks to close the specified [View].*/
class CloseViewAction(
	private val view: View<*>,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("view.action.close") {

	init {
		imagePath = "/img/close-16.png"
	}

	override fun execute(event: ActionEvent) {
		eventBus.post(CloseViewRequest(view))
	}
}