package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.OneUpCommand
import java.awt.event.ActionEvent

/**
 * An [Action] for bringing the selected [Component]s one position up in the stacking order.
 */
class OneUpAction(
        viewManager: ViewManager,
        private val cmdManager: CommandManager,
        eventBus: EventBus
) : AbstractSelectionAwareAction("edit.action.stackingOrder.oneUp", viewManager, eventBus) {

    override fun actionPerformed(e: ActionEvent?) {
        val drawing = (viewManager.activeView as DrawingView<Drawing<Component>>).drawing
        cmdManager.execute(OneUpCommand(drawing, getSelection()))
    }
}