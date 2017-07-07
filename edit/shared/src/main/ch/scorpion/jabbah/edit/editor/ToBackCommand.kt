package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * Brings a [Collection] of [Component]s to the back of the stacking order.
 */
class ToBackCommand(
        private val drawing: Drawing<Component>,
        components: Collection<Component>
) : AbstractCommand("edit.action.stackingOrder.toBack.name", null) {

    private val origStackingOrderPositions = drawing.getStackingOrderPositions(components)
    private val oldPositions = mutableMapOf<Component, Int>()

    override fun execute() {
        oldPositions.clear()
        for (pos in origStackingOrderPositions) {
            oldPositions.put(pos.drawable, drawing.getStackingOrderPosition(pos.drawable))
            drawing.setStackingOrderPosition(drawing.drawablesCount - 1, pos.drawable)
        }
    }

    override fun undo() {
        for (pos in origStackingOrderPositions.asReversed()) {
            drawing.setStackingOrderPosition(oldPositions[pos.drawable]!!, pos.drawable)
        }
    }
}