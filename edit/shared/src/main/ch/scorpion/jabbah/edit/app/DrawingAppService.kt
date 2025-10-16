package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.Clipboard
import ch.scorpion.jabbah.draw.drawable.Movable
import ch.scorpion.jabbah.draw.view.FocusDrawablePlayer
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
interface DrawingAppService : ComponentCustomizer {

	/**
	 * Adds the specified [Component] to a [DrawingView]'s [Drawing].
	 * @return the effectively added [Component]. Implementations might clone [component] before adding
	 */
	fun add(component: Component, drawingView: DrawingView<Drawing<Component>>, customizer: ComponentCustomizer? = null): Component

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
	 * Duplicates the currently selected [Component]s without using the system clipboard.
	 * @return the number of duplicated [Component]s
	 */
	fun duplicate(drawingView: DrawingView<Drawing<Component>>): Int

	/**
	 * Moves the specified [Movable]s by a given offset.
	 */
	fun move(
		movables: Collection<Movable>,
		offset: Point2D,
		editor: Editor,
		register: Boolean,
		additionalCommands: List<Command> = emptyList())
}

open class DrawingAppServiceImpl(
	private val copyPasteService: CopyPasteService = EditModule.copyPasteService,
	protected val commandManager: CommandManager = EditModule.commandManager,
	protected val eventBus: EventBus = BaseModule.eventBus
) : DrawingAppService {

	companion object {
		private val LOG by logger(DrawingAppServiceImpl::class)
	}

	override fun add(
		component: Component,
		drawingView: DrawingView<Drawing<Component>>,
		customizer: ComponentCustomizer?
	): Component {
		val command = AddCommand(drawingView, component, componentCustomizer = customizer?.let { ComponentCustomizerPair(it, this) } ?: this)
		commandManager.execute(command)
		val addedComponent = drawingView.drawing.getWithId(command.addedComponentId)!!
		drawingView.selectionManager.deselectAll()
		drawingView.selectionManager.select(addedComponent)
		return addedComponent
	}

	override fun customizeAddedComponent(component: Component, drawing: Drawing<*>) {
		// empty
	}

	override fun delete(components: List<Component>, drawingView: DrawingView<*>, cmdDescriptionKey: String?) {
		val componentIds = components.map { it.id }
		logComponentAction("Delete", componentIds, drawingView)
		commandManager.execute(DeleteCommand(drawingView, componentIds))
	}

	override fun group(components: List<Component>, drawingView: DrawingView<Drawing<Component>>) {
		require(components.size >= 2) { "grouping requires at least two Components" }
		logComponentAction("Group", components.map { it.id }, drawingView)
		val group = GroupComponent(components)
		commandManager.beginTransaction("edit.command.group", drawingView)
		components.forEach { commandManager.execute(DeleteCommand(drawingView as DrawingView<*>, it)) }
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
		logComponentAction("Cut", components.map { it.id }, drawingView)

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
		logComponentAction("Copy", drawingView.selectionManager.selection.map { it.id }, drawingView)
		Clipboard.setStringContents(copyImpl(drawingView))
	}

	private fun copyImpl(drawingView: DrawingView<*>): String
		= copyPasteService.copy(drawingView.selectionManager.selection.map { it.id }, drawingView.drawing)

	protected fun logComponentAction(action: String, componentIds: Collection<Int>, drawingView: DrawingView<*>) {
		if (componentIds.size == 1) {
			val component = drawingView.drawing.getWithId(componentIds.first())?.let {
				LOG.userTrail("$action component '${it.type}' with ID ${it.id}")
			}
		} else {
			LOG.userTrail("$action ${componentIds.size} components")
		}
	}

	override fun paste(drawingView: DrawingView<Drawing<Component>>) {
		Clipboard.getStringContents()?.let {
			LOG.userTrail("Preparing paste from clipboard")
			try {
				val pasteInfo = copyPasteService.paste(it, drawingView)
				logComponentAction("Paste", pasteInfo.componentIds, drawingView)
				if (pasteInfo.componentIds.isNotEmpty()) {
					commandManager.register(PasteCommand(drawingView, it, pasteInfo, copyPasteService))
					drawingView.selectionManager.deselectAll()
					val components = pasteInfo.componentIds.map { id -> drawingView.drawing.getWithId(id) as Component }
					drawingView.selectionManager.select(components)
					FocusDrawablePlayer.ensureVisible(components, drawingView)
				}
			} catch (e: Throwable) {
				LOG.debug("Error in paste: $e")
				// View layers might want to give feedback to the user
				throw e
			}
		}
	}

	override fun duplicate(drawingView: DrawingView<Drawing<Component>>): Int {
		logComponentAction("Duplicate", drawingView.selectionManager.selection.map { it.id }, drawingView)
		val content = copyImpl(drawingView)
		val pasteInfo = copyPasteService.paste(content, drawingView)
		if (pasteInfo.componentIds.isNotEmpty()) {
			commandManager.register(DuplicateCommand(drawingView, content, pasteInfo, copyPasteService))
			drawingView.selectionManager.deselectAll()
			drawingView.selectionManager.select(pasteInfo.componentIds.map { drawingView.drawing.getWithId(it) as Component })
		}
		return pasteInfo.componentIds.size
	}

	override fun move(
		movables: Collection<Movable>,
		offset: Point2D,
		editor: Editor,
		register: Boolean,
		additionalCommands: List<Command>
	) {
		val command = MoveCommand(editor, movables.map { it.id }.toList(), offset, additionalCommands)
		if (register) {
			additionalCommands.forEach { it.execute() }
			commandManager.register(command)
		} else {
			commandManager.execute(command)
		}
	}
}