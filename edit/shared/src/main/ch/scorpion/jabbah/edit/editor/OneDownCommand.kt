package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * Brings a [Collection] of [Component]s one level down in the stacking order
 * while maintaining their relative orders.
 */
class OneDownCommand(
        private val drawing: Drawing<Component>,
        components: Collection<Component>
) : AbstractCommand("edit.action.stackingOrder.oneDown.name", null) {

    private val origStackingOrderPositions = drawing.getStackingOrderPositions(components)

    override fun execute() {
        var i = origStackingOrderPositions.size - 1
        for (pos in origStackingOrderPositions.asReversed()) {
            if (pos.position < drawing.drawablesCount - 1) {
                val newPos = pos.position + 1
                if (i == origStackingOrderPositions.size - 1 || newPos < drawing.getStackingOrderPosition(origStackingOrderPositions[i + 1].drawable)) {
                    drawing.setStackingOrderPosition(newPos, pos.drawable)
                }
            }
            i--
        }
    }

    override fun undo() {
        for (pos in origStackingOrderPositions.asReversed()) {
            drawing.setStackingOrderPosition(pos.position, pos.drawable)
        }
    }
}