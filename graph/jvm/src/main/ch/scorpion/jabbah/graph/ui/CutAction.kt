package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * An [Action] for cutting the selected [Component]s from the [Drawing] of the current
 * [View] to the clipboard.
 */
class CutAction(
    eventBus: EventBus = BaseModule.eventBus,
    viewManager: ViewManager = DrawViewModule.viewManager,
    val typeMap: TypeMap = IOModule.typeMap,
    val cmdManager: CommandManager = EditModule.commandManager
) : AbstractSelectionAwareAction("edit.action.cut", eventBus, viewManager) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        val drawingView = viewManager.activeView as DrawingView<Drawing<Component>>
        CopyPasteUtility.cut(
                drawingView,
                drawingView.selectionManager.selection,
                typeMap,
                cmdManager)
    }
}