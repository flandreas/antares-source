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
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * An [Action] for deleting the selected [Component]s in a [Drawing].
 */
class DeleteAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val drawingService: DrawingService = EditModule.drawingService
) : AbstractSelectionAwareAction("edit.action.delete", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.activeView as DrawingView<Drawing<Component>>
		drawingService.delete(
			drawingView.selectionManager.selection
				.filter { it.deletable }
				.toCollection(mutableListOf<Component>()),
			drawingView)
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