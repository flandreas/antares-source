package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.draw.module.DrawModule

/** An [Action] for toggling the graphics debug mode.*/
class DebugGraphicsAction : AbstractViewAction("view.action.debugGraphics") {

	override fun execute(event: ActionEvent) {
		DrawModule.debugGfx = !DrawModule.debugGfx
		viewManager.activeView?.view?.repaint()
	}
}