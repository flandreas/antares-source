package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.app.DrawingService
import ch.scorpion.jabbah.edit.module.EditModule
import javax.swing.Action
import java.awt.event.ActionEvent

/**
 * An [Action] for deleting the selected [Component]s in a [Drawing].
 */
class DeleteAction(
        viewManager: ViewManager,
        private val drawingService: DrawingService = EditModule.drawingService
) : AbstractSelectionAwareAction("edit.action.delete", viewManager) {

    override fun actionPerformed(e: ActionEvent?) {
        val drawingView = viewManager.activeView as DrawingView<Drawing<Component>>
        drawingService.delete(
                drawingView.selectionManager.selection
                        .filter { it.deletable }
                        .toCollection(mutableListOf<Component>()),
                drawingView)
    }
}