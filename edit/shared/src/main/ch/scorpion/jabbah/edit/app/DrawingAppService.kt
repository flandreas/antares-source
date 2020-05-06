package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.group.GroupComponent
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * An application service for [Drawing] that enhances the domain services and classes with
 * undo/redo functionality.
 */
interface DrawingAppService {

	/**
	 * Adds the specified [Component] to a [DrawingView]'s [Drawing].
	 * @return the effectively added [Component]. Implementations might clone [component] before adding
	 */
	fun add(component: Component, drawingView: DrawingView<Drawing<Component>>): Component

	/**
	 * Deletes the specified [Component] from its [Drawing].
	 * @param cmdDescriptionKey the translation key of the [Command] that makes this operation undoable,
	 * or `null` to use the default name
	 */
	fun delete(components: List<Component>, drawingView: DrawingView<Drawing<Component>>, cmdDescriptionKey: String? = null)

	/** Replaces the specified [Component]s with a newly created [GroupComponent].*/
	fun group(components: List<Component>, drawingView: DrawingView<Drawing<Component>>)

	/** Replaces the specified [GroupComponent] in the [DrawingView] with its inner [Component]s.*/
	fun ungroup(component: GroupComponent, drawingView: DrawingView<Drawing<Component>>)

	/** Cuts the [Component]s that are currently selected in [drawingView] to the system clipboard.*/
	fun cut(drawingView: DrawingView<Drawing<Component>>)

	/** Copies the [Component]s that are currently selected in [drawingView] to the system clipboard.*/
	fun copy(drawingView: DrawingView<Drawing<Component>>)

	/** Pastes the current contents of the system clipboard into the specified [DrawingView].*/
	fun paste(drawingView: DrawingView<Drawing<Component>>)
}

open class DrawingAppServiceImpl(
	protected val copyPasteService: CopyPasteService = EditModule.copyPasteService,
	protected val commandManager: CommandManager = EditModule.commandManager,
	protected val eventBus: EventBus = BaseModule.eventBus
) : DrawingAppService {

	override fun add(component: Component, drawingView: DrawingView<Drawing<Component>>): Component {
		val command = AddCommand(drawingView, component)
		commandManager.execute(command)
		val addedComponent = drawingView.drawing.getWithId(command.addedComponentId)!!
		drawingView.selectionManager.deselectAll()
		drawingView.selectionManager.select(addedComponent)
		return addedComponent
	}

	override fun delete(components: List<Component>, drawingView: DrawingView<Drawing<Component>>, cmdDescriptionKey: String?) {
		commandManager.execute(DeleteCommand(drawingView, components.map { it.id }))
	}

	override fun group(components: List<Component>, drawingView: DrawingView<Drawing<Component>>) {
		checkArgument(components.size >= 2, "grouping requires at least two Components")
		val group = GroupComponent(components)
		commandManager.beginTransaction("edit.command.group", drawingView)
		components.forEach { commandManager.execute(DeleteCommand(drawingView, it)) }
		add(group, drawingView)
		commandManager.commitTransaction()
	}

	override fun ungroup(component: GroupComponent, drawingView: DrawingView<Drawing<Component>>) {
		commandManager.beginTransaction("edit.command.ungroup", drawingView)
		delete(listOf(component), drawingView)
		component.components.asReversed().forEach { commandManager.execute(AddCommand(drawingView, it)) }
		commandManager.commitTransaction()
		drawingView.selectionManager.select(component.components)
	}

	override fun cut(drawingView: DrawingView<Drawing<Component>>) {
		val components = drawingView.selectionManager.selection
		val componentsToDelete = components.filter { it.deletable }.toList()
		if (componentsToDelete.isNotEmpty()) {
			copy(drawingView)
			delete(componentsToDelete, drawingView, "edit.command.cut")
		}

		// Don't do 'components.size != selection.size for checking whether everything has been deleted,
		// because non-deletable (by user selection!) Components might have been deleted as a side effect
		// of deleting other Components.
		if (components.any { drawingView.drawing.contains(it) }) {
			eventBus.post(ComponentMessage(
				ComponentMessageType.Info,
				null,
				"edit.action.undeletable.msg"
			))
		}
	}

	override fun copy(drawingView: DrawingView<Drawing<Component>>) {
		Clipboard.setStringContents(copyPasteService.copy(drawingView.selectionManager.selection.map { it.id }, drawingView.drawing))
	}

	override fun paste(drawingView: DrawingView<Drawing<Component>>) {
		Clipboard.getStringContents()?.let {
			val pasteInfo = copyPasteService.paste(it, drawingView)
			commandManager.register(PasteCommand(drawingView, it, pasteInfo, copyPasteService))
			drawingView.selectionManager.deselectAll()
			drawingView.selectionManager.select(pasteInfo.components)
		}
	}
}