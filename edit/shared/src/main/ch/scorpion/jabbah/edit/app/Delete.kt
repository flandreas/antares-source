package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractDrawingViewCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * An [Action] for deleting the selected [Component]s in a [Drawing].
 */
class DeleteAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	private val service: DrawingAppService = EditModule.drawingAppService
) : AbstractSelectionAwareAction("edit.action.delete", eventBus, viewManager) {

	companion object {

		/** Creates a new [List] containing only those [Component] that can really be deleted.*/
		fun getComponentsToDelete(components: Collection<Component>): List<Component> {
			return components.filter { it.deletable }.toCollection(mutableListOf())
		}
	}

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.activeView!!.view as DrawingView<*>
		val selection = drawingView.selectionManager.selection
		val components = getComponentsToDelete(selection)
		if (components.isNotEmpty()) {
			service.delete(
				components,
				drawingView)
		}

		// Don't do 'components.size != selection.size for checking whether everything has been deleted,
		// because non-deletable (by user selection!) Components might have been deleted as a side effect
		// of deleting other Components.
		if (selection.any { drawingView.drawing.contains(it) }) {
			eventBus.post(ComponentMessage(
				ComponentMessageType.Info,
				null,
				"edit.action.undeletable.msg"
			))
		}
	}
}

/**
 * A [Command] for deleting the selected [Component]s from a [Drawing].
 * Doesn't implement [Undoable] by intention to avoid creating clones for all deleted [Component]s.
 * TODO How can we preserve the original stacking order of the removed Components?
 */
class DeleteCommand(
	drawingView: DrawingView<*>,
	private val componentIds: List<Int>
) : AbstractDrawingViewCommand("edit.command.delete", drawingView) {

	constructor(drawingView: DrawingView<*>, component: Component) : this(drawingView, mutableListOf(component.id))

	override fun getDetailedDescription(): String =
		if (componentIds.size == 1) {
			val id = componentIds.first()
			val component = view.drawing.getWithId(id)!!
			"${super.getDetailedDescription()} ${component::class.simpleName} $id"
		} else {
			"${super.getDetailedDescription()} ${componentIds.size} components"
		}

	override fun execute() {
		view.drawing.remove(componentIds)
	}
}