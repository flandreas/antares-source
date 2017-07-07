package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import java.awt.event.ActionEvent

/**
 * An [Action] for selection all [Component]s in a [Drawing].
 */
class SelectAllAction(
    eventBus: EventBus,
    viewManager: ViewManager
) : AbstractViewAction("edit.action.selectAll", eventBus, viewManager) {

    override fun actionPerformed(e: ActionEvent?) {
        (viewManager.activeView as DrawingView<*>).selectionManager.selectAll()
        viewManager.activeView!!.repaint()
    }
}