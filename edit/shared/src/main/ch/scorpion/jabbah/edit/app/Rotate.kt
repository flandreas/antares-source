package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.drawable.Rotatable
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractDrawingViewCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.module.EditModule

class RotateAction(
	val clockwise: Boolean = false,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractSelectionAwareAction(if (clockwise) "edit.action.rotateClockwise" else ACTION_KEY, eventBus, viewManager) {

	companion object {
		private val LOG by logger(RotateAction::class)
		const val ACTION_KEY = "edit.action.rotate"
	}

	override fun execute(event: ActionEvent) {
		if (!selection.let { sel -> sel.all { it.isRotatableWith(sel) } }) {
			eventBus.post(ComponentMessage(
				ComponentMessageType.Error,
				null,
				"edit.action.rotate.denied.msg"))
			return
		}

		LOG.userTrail("Rotate $selectionCount components, clockwise = $clockwise")

		commandManager.execute(
			RotateCommand(
				clockwise,
				drawingView!!,
				selection.map { it.id },
				selection.first().location
			)
		)
	}
}

/** Rotates a [Component] to the given [Rotation].*/
class RotateCommand(
	private val clockwise: Boolean,
	drawingView: DrawingView<*>,
	val componentIds: Collection<Int>,
	val pivot: Point2D? = null
) : AbstractDrawingViewCommand(if (clockwise) "edit.command.rotateClockwise" else "edit.command.rotate", drawingView), Undoable {

	private val components: Collection<Component> get() = componentIds.map { view.drawing.getWithId(it)!! }.toList()

	override fun execute() {
		Rotatable.rotate(components, RotationDirection.of(clockwise), pivot)
	}

	override fun undo() {
		Rotatable.rotate(components, RotationDirection.notOf(clockwise), pivot)
	}
}