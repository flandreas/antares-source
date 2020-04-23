package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule

class ToFrontAction(
	private val cmdManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.stackingOrder.toFront", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawing = viewManager.castedActiveView<DrawingView<Drawing<Component>>>()!!.drawing
		cmdManager.execute(ToFrontCommand(drawing, selection))
	}
}

class OneUpAction(
	private val cmdManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.stackingOrder.oneUp", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawing = viewManager.castedActiveView<DrawingView<Drawing<Component>>>()!!.drawing
		cmdManager.execute(OneUpCommand(drawing, selection))
	}
}

class OneDownAction(
	private val cmdManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.stackingOrder.oneDown", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawing = viewManager.castedActiveView<DrawingView<Drawing<Component>>>()!!.drawing
		cmdManager.execute(OneDownCommand(drawing, selection))
	}
}

class ToBackAction(
	private val cmdManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.stackingOrder.toBack", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawing = viewManager.castedActiveView<DrawingView<Drawing<Component>>>()!!.drawing
		cmdManager.execute(ToBackCommand(drawing, selection))
	}
}

internal abstract class StackingOrderCommand(
	name: String,
	protected val drawing: Drawing<Component>
) : AbstractCommand(name, null) {

	override fun validate() {
		drawing.validate()
	}
}

/**
 * Brings a [Collection] of [Component]s to the front of the stacking order.
 */
internal class ToFrontCommand(
	drawing: Drawing<Component>,
	components: Collection<Component>
) : StackingOrderCommand("edit.action.stackingOrder.toFront.name", drawing) {

	private val origStackingOrderPositions = drawing.getStackingOrderPositions(components)
	private val oldPositions = mutableMapOf<Component, Int>()

	override fun execute() {
		oldPositions.clear()
		for ((i, pos) in origStackingOrderPositions.withIndex()) {
			oldPositions[pos.drawable] = drawing.getStackingOrderPosition(pos.drawable)
			drawing.setStackingOrderPosition(i, pos.drawable)
		}
	}

	override fun undo() {
		for (pos in origStackingOrderPositions.asReversed()) {
			drawing.setStackingOrderPosition(oldPositions[pos.drawable]!!, pos.drawable)
		}
	}
}

/**
 * Brings a [Collection] of [Component]s one level up in the stacking order
 * while maintaining their relative orders.
 */
internal class OneUpCommand(
	drawing: Drawing<Component>,
	components: Collection<Component>
) : StackingOrderCommand("edit.action.stackingOrder.oneUp.name", drawing) {

	private val origStackingOrderPositions = drawing.getStackingOrderPositions(components)

	override fun execute() {
		for ((i, pos) in origStackingOrderPositions.withIndex()) {
			if (pos.position > 0) {
				val newPos = pos.position - 1
				if (i == 0 || newPos > drawing.getStackingOrderPosition(origStackingOrderPositions[i - 1].drawable)) {
					drawing.setStackingOrderPosition(newPos, pos.drawable)
				}
			}
		}
	}

	override fun undo() {
		for (pos in origStackingOrderPositions.asReversed()) {
			drawing.setStackingOrderPosition(pos.position, pos.drawable)
		}
	}
}

/**
 * Brings a [Collection] of [Component]s one level down in the stacking order
 * while maintaining their relative orders.
 */
internal class OneDownCommand(
	drawing: Drawing<Component>,
	components: Collection<Component>
) : StackingOrderCommand("edit.action.stackingOrder.oneDown.name", drawing) {

	private val origStackingOrderPositions = drawing.getStackingOrderPositions(components)

	override fun execute() {
		var i = origStackingOrderPositions.size - 1
		for (pos in origStackingOrderPositions.asReversed()) {
			if (pos.position < drawing.drawablesCount - 1) {
				val newPos = pos.position + 1
				if (i == origStackingOrderPositions.size - 1 || newPos < drawing.getStackingOrderPosition(origStackingOrderPositions[i + 1].drawable)) {
					drawing.setStackingOrderPosition(newPos, pos.drawable)
				}
			}
			i--
		}
	}

	override fun undo() {
		for (pos in origStackingOrderPositions.asReversed()) {
			drawing.setStackingOrderPosition(pos.position, pos.drawable)
		}
	}
}

/**
 * Brings a [Collection] of [Component]s to the back of the stacking order.
 */
internal class ToBackCommand(
	drawing: Drawing<Component>,
	components: Collection<Component>
) : StackingOrderCommand("edit.action.stackingOrder.toBack.name", drawing) {

	private val origStackingOrderPositions = drawing.getStackingOrderPositions(components)
	private val oldPositions = mutableMapOf<Component, Int>()

	override fun execute() {
		oldPositions.clear()
		for (pos in origStackingOrderPositions) {
			oldPositions[pos.drawable] = drawing.getStackingOrderPosition(pos.drawable)
			drawing.setStackingOrderPosition(drawing.drawablesCount - 1, pos.drawable)
		}
	}

	override fun undo() {
		for (pos in origStackingOrderPositions.asReversed()) {
			drawing.setStackingOrderPosition(oldPositions[pos.drawable]!!, pos.drawable)
		}
	}
}