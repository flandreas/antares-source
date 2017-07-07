package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * Rotates a [Component] by a given angle.
 */
class RotateCommand(
        val component: Component,
        val rotation: Rotation): AbstractCommand("edit.command.rotate", null) {

    override fun execute() {
        component.rotation = rotation
    }

    override fun undo() {
        component.rotation = oldRotation
    }

    private val oldRotation = component.rotation
}