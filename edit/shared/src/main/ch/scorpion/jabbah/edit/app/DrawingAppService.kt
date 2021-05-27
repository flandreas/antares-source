package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.Clipboard
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.group.GroupComponent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.MoveCommand

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
	fun delete(components: List<Component>, drawingView: DrawingView<*>, cmdDescriptionKey: String? = null)

	/** Replaces the specified [Component]s with a newly created [GroupComponent].*/
	fun group(components: List<Component>, drawingView: DrawingView<Drawing<Component>>)

	/** Replaces the specified [GroupComponent] in the [DrawingView] with its inner [Component]s.*/
	fun ungroup(component: GroupComponent, drawingView: DrawingView<Drawing<Component>>)

	/** Cuts the [Component]s that are currently selected in [drawingView] to the system clipboard.*/
	fun cut(drawingView: DrawingView<*>)

	/** Copies the [Component]s that are currently selected in [drawingView] to the system clipboard.*/
	fun copy(drawingView: DrawingView<*>)

	/**
	 * Pastes the current contents of the system clipboard into the specified [DrawingView].
	 * @throws IllegalArgumentException if the clipboard content could not be parsed
	 */
	fun paste(drawingView: DrawingView<Drawing<Component>>)

	/**
	 * Moves the specified [Movable]s by a given offset.
	 */
	fun move(movables: Collection<Movable>, offset: Point2D, editor: Editor, register: Boolean)
}

open class DrawingAppServiceImpl(
	private val copyPasteService: CopyPasteService = EditModule.copyPasteService,
	protected val commandManager: CommandManager = EditModule.commandManager,
	protected val eventBus: EventBus = BaseModule.eventBus
) : DrawingAppService {

	companion object {
		private val LOG by logger(DrawingAppServiceImpl::class)
	}

	override fun add(component: Component, drawingView: DrawingView<Drawing<Component>>): Component {
		val command = AddCommand(drawingView, component, componentCustomizer = ::customizeAddedComponent)
		commandManager.execute(command)
		val addedComponent = drawingView.drawing.getWithId(command.addedComponentId)!!
		drawingView.selectionManager.deselectAll()
		drawingView.selectionManager.select(addedComponent)
		return addedComponent
	}

	/**
	 * Used by method that add [Component]s to a [Drawing] (and the corresponding [Command]s)
	 * to customize the properties of the [Component] after it has been added to the [Drawing].
	 * Can for example be used to apply default from the [Drawing] (such as default colors)
	 * to added [Component]s.
	 */
	protected open fun customizeAddedComponent(component: Component) {
		// empty
	}

	override fun delete(components: List<Component>, drawingView: DrawingView<*>, cmdDescriptionKey: String?) {
		logComponentAction("Delete", components)
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
		ungroupImpl(component, component, drawingView)
	}

	protected fun ungroupImpl(component: GroupComponent, owner: Component, drawingView: DrawingView<Drawing<Component>>) {
		commandManager.beginTransaction("edit.command.ungroup", drawingView)
		delete(listOf(owner), drawingView)

		val addedComponentIds = mutableListOf<Int>()
		component.components.asReversed().forEach {
			val command = AddCommand(drawingView, it)
			commandManager.execute(command)
			addedComponentIds.add(command.addedComponentId)
		}
		commandManager.commitTransaction()

		drawingView.selectionManager.select(addedComponentIds.map { drawingView.drawing.getWithId(it) as Component })
	}

	override fun cut(drawingView: DrawingView<*>) {
		val components = drawingView.selectionManager.selection
		logComponentAction("Cut", components)

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

	override fun copy(drawingView: DrawingView<*>) {
		logComponentAction("Copy", drawingView.selectionManager.selection)
		Clipboard.setStringContents(copyPasteService.copy(drawingView.selectionManager.selection.map { it.id }, drawingView.drawing))
	}

	protected fun logComponentAction(action: String, components: Collection<Component>) {
		if (components.size == 1) {
			LOG.debug("$action component '${components.first().type}' with ID ${components.first().id}")
		} else {
			LOG.debug("$action ${components.size} components")
		}
	}

	override fun paste(drawingView: DrawingView<Drawing<Component>>) {
		Clipboard.getStringContents()?.let {
			LOG.debug("Preparing paste from clipboard")
			val pasteInfo = copyPasteService.paste(it, drawingView)
			logComponentAction("Paste", pasteInfo.components)
			commandManager.register(PasteCommand(drawingView, it, pasteInfo, copyPasteService))
			drawingView.selectionManager.deselectAll()
			drawingView.selectionManager.select(pasteInfo.components)
		}
	}

	override fun move(movables: Collection<Movable>, offset: Point2D, editor: Editor, register: Boolean) {
		val command = if (movables.size == 1) {
			movables.first().getMoveCommand(editor, offset)
		} else {
			MoveCommand(editor, movables.map { it.id }.toList(), offset)
		}
		if (register) {
			commandManager.register(command)
		} else {
			commandManager.execute(command)
		}
	}
}