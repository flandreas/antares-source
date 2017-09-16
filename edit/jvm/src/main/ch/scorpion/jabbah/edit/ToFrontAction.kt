package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.ToFrontCommand
import java.awt.event.ActionEvent

/**
 * An [Action] for bringing the selected [Component]s to the front of the stacking order.
 */
class ToFrontAction(
        viewManager: ViewManager,
        private val cmdManager: CommandManager,
        eventBus: EventBus
) : AbstractSelectionAwareAction("edit.action.stackingOrder.toFront", viewManager, eventBus) {

    override fun actionPerformed(e: ActionEvent?) {
        val drawing = (viewManager.activeView as DrawingView<Drawing<Component>>).drawing
        cmdManager.execute(ToFrontCommand(drawing, getSelection()))
    }
}