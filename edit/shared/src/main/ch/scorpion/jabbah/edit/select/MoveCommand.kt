package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Moves a collection of [Component]s in a [Drawing]
 */
class MoveCommand(
    editor: Editor,
    val components: Collection<Component>,
    val offset: Point2D
) : AbstractCommand("edit.command.move", editor) {

    override fun execute() {
        components.forEach { it.prepareMoveBy(components) }
        components.forEach { it.moveBy(offset.x, offset.y) }
        components.forEach { it.completeMoveBy() }
    }

    override fun undo() {
        components.forEach { it.prepareMoveBy(components) }
        components.forEach { it.moveBy(-offset.x, -offset.y) }
        components.forEach { it.completeMoveBy() }
    }
}