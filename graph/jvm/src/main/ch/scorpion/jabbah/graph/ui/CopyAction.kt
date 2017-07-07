package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap
import java.awt.event.ActionEvent

/**
 * An [Action] for copying the selected [Component]s to the clipboard.
 */
class CopyAction(
    eventBus: EventBus,
    viewManager: ViewManager,
    val typeMap: TypeMap
) : AbstractSelectionAwareAction("edit.action.copy", viewManager, eventBus) {

    constructor(): this(BaseModule.eventBus, DrawViewModule.viewManager, IOModule.typeMap)

    override fun actionPerformed(e: ActionEvent?) {
        val drawingView = viewManager.activeView as DrawingView<*>
        CopyPasteUtility.copy(
            drawingView.drawing as GraphView<*>,
            drawingView.selectionManager.selection,
            typeMap)
    }
}