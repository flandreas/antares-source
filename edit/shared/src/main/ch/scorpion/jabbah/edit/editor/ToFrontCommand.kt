package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * Brings a [Collection] of [Component]s to the front of the stacking order.
 */
class ToFrontCommand(
    private val drawing: Drawing<Component>,
    components: Collection<Component>
) : AbstractCommand("edit.action.stackingOrder.toFront.name", null) {

    private val origStackingOrderPositions = drawing.getStackingOrderPositions(components)
    private val oldPositions = mutableMapOf<Component, Int>()

    override fun execute() {
        oldPositions.clear()
        for ((i, pos) in origStackingOrderPositions.withIndex()) {
            oldPositions.put(pos.drawable, drawing.getStackingOrderPosition(pos.drawable))
            drawing.setStackingOrderPosition(i, pos.drawable)
        }
    }

    override fun undo() {
        for (pos in origStackingOrderPositions.asReversed()) {
            drawing.setStackingOrderPosition(oldPositions[pos.drawable]!!, pos.drawable)
        }
    }
}