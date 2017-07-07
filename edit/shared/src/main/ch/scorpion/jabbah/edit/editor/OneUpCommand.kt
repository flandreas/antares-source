package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * Brings a [Collection] of [Component]s one level up in the stacking order
 * while maintaining their relative orders.
 */
class OneUpCommand(
        private val drawing: Drawing<Component>,
        components: Collection<Component>
) : AbstractCommand("edit.action.stackingOrder.oneUp.name", null) {

    private val origStackingOrderPositions = drawing.getStackingOrderPositions(components)

    override fun execute() {
        for ((i, pos) in origStackingOrderPositions.withIndex()) {
            if (pos.position > 0) {
                val newPos = pos.position - 1
                if (i == 0 || newPos > drawing.getStackingOrderPosition(origStackingOrderPositions[i - 1].drawable)) {
                    drawing.setStackingOrderPosition(newPos, pos.drawable)
                }
            }
        }
    }

    override fun undo() {
        for (pos in origStackingOrderPositions.asReversed()) {
            drawing.setStackingOrderPosition(pos.position, pos.drawable)
        }
    }
}