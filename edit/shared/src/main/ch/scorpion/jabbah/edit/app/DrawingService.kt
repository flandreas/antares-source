package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.edit.model.group.GroupComponent
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

	/** Replaces the specified [Component]s with a newly created [GroupComponent].*/
    fun group(components: List<Component>, drawingView: DrawingView<Drawing<Component>>)

	/** Replaces the specified [GroupComponent] in the [DrawingView] with its inner [Component]s.*/
	fun ungroup(component: GroupComponent, drawingView: DrawingView<Drawing<Component>>)
}

open class DrawingServiceImpl(
        private val commandManager: CommandManager = EditModule.commandManager
) : DrawingService {

	/** ---- [DrawingService] interface */

    override fun delete(components: List<Component>, drawingView: DrawingView<Drawing<Component>>) {
        commandManager.execute(DeleteCommand(drawingView, components))
    }

	override fun group(components: List<Component>, drawingView: DrawingView<Drawing<Component>>) {
		checkArgument(components.size >= 2, "grouping requires at least two Components")
		val group = GroupComponent(components)
		commandManager.beginTransaction("edit.command.group", drawingView)
		components.forEach { commandManager.execute(DeleteCommand(drawingView, it)) }
		commandManager.execute(AddCommand(drawingView, group))
		commandManager.commitTransaction()
		drawingView.selectionManager.select(group)
	}

	override fun ungroup(component: GroupComponent, drawingView: DrawingView<Drawing<Component>>) {
		commandManager.beginTransaction("edit.command.ungroup", drawingView)
		commandManager.execute(DeleteCommand(drawingView, component))
		component.components.asReversed().forEach { commandManager.execute(AddCommand(drawingView, it)) }
		commandManager.commitTransaction()
		drawingView.selectionManager.select(component.components)
	}
}