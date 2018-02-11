package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing

/** An [Action] for selecting all [Component]s in a [Drawing].*/
class SelectAllAction(
        eventBus: EventBus = BaseModule.eventBus,
        viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractViewAction("edit.action.selectAll", eventBus, viewManager) {

    override fun execute(event: ActionEvent) {
        (viewManager.activeView as DrawingView<*>).selectionManager.selectAll()
        viewManager.activeView!!.repaint()
    }
}