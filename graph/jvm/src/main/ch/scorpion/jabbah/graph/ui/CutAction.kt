package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap
import java.awt.event.ActionEvent

/**
 * An [Action] for cutting the selected [Component]s from the [Drawing] of the current
 * [View] to the clipboard.
 */
class CutAction(
    eventBus: EventBus,
    viewManager: ViewManager,
    val typeMap: TypeMap,
    val cmdManager: CommandManager
) : AbstractSelectionAwareAction("edit.action.cut", viewManager, eventBus) {

    constructor(): this(BaseModule.eventBus, DrawViewModule.viewManager, IOModule.typeMap, EditModule.commandManager)

    override fun actionPerformed(e: ActionEvent?) {
        val drawingView = viewManager.activeView as DrawingView<Drawing<Component>>
        CopyPasteUtility.cut(
            drawingView,
            drawingView.selectionManager.selection,
            typeMap,
            cmdManager)
    }
}