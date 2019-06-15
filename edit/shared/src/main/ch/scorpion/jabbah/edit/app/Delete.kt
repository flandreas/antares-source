package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * An [Action] for deleting the selected [Component]s in a [Drawing].
 */
class DeleteAction(
	private val eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val drawingService: DrawingService = EditModule.drawingService
) : AbstractSelectionAwareAction("edit.action.delete", eventBus, viewManager) {

	companion object {

		/** Creates a new [List] containing only those [Component] that can really be deleted.*/
		fun getComponentsToDelete(components: Collection<Component>): List<Component> {
			return components.filter { it.deletable }.toCollection(mutableListOf())
		}
	}

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.castedActiveView<DrawingView<Drawing<Component>>>()!!
		val selection = drawingView.selectionManager.selection
		val components = getComponentsToDelete(selection)
		if (components.isNotEmpty()) {
			drawingService.delete(
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
 * TODO How can we preserve the original stacking order of the removed Components?
 */
class DeleteCommand(
	val drawingView: DrawingView<Drawing<Component>>,
	private val components: List<Component>
) : AbstractCommand("edit.command.delete", null) {

	constructor(drawingView: DrawingView<Drawing<Component>>, component: Component) : this(drawingView, mutableListOf(component))

	override fun execute() {
		for (c in components) {
			drawingView.drawing.remove(c)
		}
	}

	override fun undo() {
		for (c in components) {
			drawingView.drawing.add(c)
		}
		drawingView.selectionManager.select(components)
	}

	override fun validate() {
		drawingView.drawing.validate()
	}
}