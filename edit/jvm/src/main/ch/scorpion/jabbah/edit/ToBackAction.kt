package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.ToBackCommand
import java.awt.event.ActionEvent

/**
 * An [Action] for bringing the selected [Component]s to the back of the stacking order.
 */
class ToBackAction(
        viewManager: ViewManager,
        private val cmdManager: CommandManager,
        eventBus: EventBus
) : AbstractSelectionAwareAction("edit.action.stackingOrder.toBack", viewManager, eventBus) {

    override fun actionPerformed(e: ActionEvent?) {
        val drawing = (viewManager.activeView as DrawingView<Drawing<Component>>).drawing
        cmdManager.beginTransaction(ToBackCommand(drawing, getSelection()))
        cmdManager.commitTransaction()
    }
}