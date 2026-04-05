package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.CloseViewRequest
import io.antarescircuit.jabbah.draw.View

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