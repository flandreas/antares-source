package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.event.VetoException
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractDrawingViewCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.DrawingService
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * Posted by [DeleteAction] on its [EventBus] to ask if anybody wants to forbid deletion of any [Component] in [components].
 * Note that differs from the context [Component.deletable], which depends on the type or state of the [Component],
 * while [DeleteQuestion] is rather answers by higher-level objects like controllers.
 *
 * A forbidding object should throw a [VetoException] and indicate the first [Component] for which deletion is
 * to be denied. This is used for targeting the resulting [ComponentMessage] posted by [DeleteAction]
 */
data class DeleteQuestion(
	val components: Collection<Component>,
	val drawingView: DrawingView<*>,
)

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

		eventBus.postTwoPhase(
			DeleteQuestion(components, drawingView),
			thenHandler = {
				executeImpl(components, drawingView)
			},
			elseHandler = {
				postUndeleteableMessage(it.source as? Component)
			}
		)
	}

	private fun executeImpl(components: List<Component>, drawingView: DrawingView<*>) {
		if (components.isNotEmpty()) {
			service.delete(
				components,
				drawingView)
		}

		// Don't do components.size != selection.size for checking whether everything has been deleted,
		// because non-deletable (by user selection!) Components might have been deleted as a side effect
		// of deleting other Components.
		if (selection.any { drawingView.drawing.contains(it) }) {
			postUndeleteableMessage()
		}
	}

	private fun postUndeleteableMessage(source: Component? = null) {
		eventBus.post(ComponentMessage(
			ComponentMessageType.Info,
			source = source,
			"edit.action.undeletable.msg"
		))
	}
}

/**
 * A [Command] for deleting the selected [Component]s from a [Drawing].
 * Doesn't implement [Undoable] by intention to avoid creating clones for all deleted [Component]s.
 * TODO How can we preserve the original stacking order of the removed Components?
 */
class DeleteCommand(
	drawingView: DrawingView<*>,
	private val componentIds: List<Int>,
	cmdDescriptionKey: String? = null,
	private val drawingService: DrawingService = EditModule.drawingService
) : AbstractDrawingViewCommand(cmdDescriptionKey ?: "edit.command.delete", drawingView) {

	constructor(drawingView: DrawingView<*>, component: Component) : this(drawingView, mutableListOf(component.id))

	override fun getDetailedDescription(): String =
		if (componentIds.size == 1) {
			val id = componentIds.first()
			"${super.getDetailedDescription()} $id"
		} else {
			"${super.getDetailedDescription()} ${componentIds.size} components"
		}

	override fun execute() {
		drawingService.delete(view.drawing.getWidthIds(componentIds).toList(), view.drawing)
	}
}