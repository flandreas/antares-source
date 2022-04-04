package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.draw.module.DrawModule

/** An [Action] for toggling the graphics debug mode.*/
class DebugGraphicsAction : AbstractViewAction("view.action.debugGraphics") {

	override fun execute(event: ActionEvent) {
		DrawModule.debugGfx = !DrawModule.debugGfx
		viewManager.activeView?.view?.repaint()
	}
}