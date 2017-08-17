package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * An application service for [Drawing] that enhances the domain services and classes with
 * undo/redo functionality.
 */
interface DrawingService {

    /**
     * Deletes the specified [Component] from its [Drawing].
     */
    fun delete(components: List<Component>, drawingView: DrawingView<Drawing<Component>>)
}

open class DrawingServiceImpl(
        private val commandManager: CommandManager = EditModule.commandManager
) : DrawingService {

    /** ---- [DrawingService] interface */

    override fun delete(components: List<Component>, drawingView: DrawingView<Drawing<Component>>) {
        commandManager.beginTransaction(DeleteCommand(drawingView, components))
        commandManager.commitTransaction()
    }
}