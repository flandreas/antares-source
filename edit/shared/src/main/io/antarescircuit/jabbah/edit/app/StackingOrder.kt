package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.command.AbstractDrawingViewCommand
import io.antarescircuit.jabbah.edit.module.EditModule

class ToFrontAction(
	private val cmdManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.stackingOrder.toFront", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		cmdManager.execute(ToFrontCommand(drawingView!!, selection.map { it.id }))
	}
}

class OneUpAction(
	private val cmdManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.stackingOrder.oneUp", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		cmdManager.execute(OneUpCommand(drawingView!!, selection.map { it.id }))
	}
}

class OneDownAction(
	private val cmdManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.stackingOrder.oneDown", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		cmdManager.execute(OneDownCommand(drawingView!!, selection.map { it.id }))
	}
}

class ToBackAction(
	private val cmdManager: CommandManager = EditModule.commandManager,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.stackingOrder.toBack", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		cmdManager.execute(ToBackCommand(drawingView!!, selection.map { it.id }))
	}
}

abstract class StackingOrderCommand(
	name: String,
	drawingView: DrawingView<*,*>,
	protected val componentIds: Collection<Int>
) : AbstractDrawingViewCommand(name, drawingView) {

	protected val drawing: Drawing<*> get() = view.drawing
	protected val components get() = componentIds.map { drawing.getWithId(it) as Component }
}

/**
 * Brings a [Collection] of [Component]s to the front of the stacking order.
 */
class ToFrontCommand(
	drawingView: DrawingView<*,*>,
	componentIds: Collection<Int>
) : StackingOrderCommand("edit.action.stackingOrder.toFront.name", drawingView, componentIds), Undoable {

	private val origStackingOrderPositions = drawing.getStackingOrderPositions(componentIds)
	// Maps Component ID to the position index
	private val oldPositions = mutableMapOf<Int, Int>()

	override fun execute() {
		oldPositions.clear()
		for ((i, pos) in origStackingOrderPositions.withIndex()) {
			oldPositions[pos.componentId] = drawing.getStackingOrderPosition(pos.componentId)
			drawing.setStackingOrderPosition(i, pos.componentId)
		}
	}

	override fun undo() {
		for (pos in origStackingOrderPositions.asReversed()) {
			drawing.setStackingOrderPosition(oldPositions[pos.componentId]!!, pos.componentId)
		}
	}
}

/**
 * Brings a [Collection] of [Component]s one level up in the stacking order
 * while maintaining their relative orders.
 */
class OneUpCommand(
	drawingView: DrawingView<*,*>,
	componentIds: Collection<Int>
) : StackingOrderCommand("edit.action.stackingOrder.oneUp.name", drawingView, componentIds), Undoable {

	private val origStackingOrderPositions = drawing.getStackingOrderPositions(componentIds)

	override fun execute() {
		for ((i, pos) in origStackingOrderPositions.withIndex()) {
			if (pos.position > 0) {
				val newPos = pos.position - 1
				if (i == 0 || newPos > drawing.getStackingOrderPosition(origStackingOrderPositions[i - 1].componentId)) {
					drawing.setStackingOrderPosition(newPos, pos.componentId)
				}
			}
		}
	}

	override fun undo() {
		for (pos in origStackingOrderPositions.asReversed()) {
			drawing.setStackingOrderPosition(pos.position, pos.componentId)
		}
	}
}

/**
 * Brings a [Collection] of [Component]s one level down in the stacking order
 * while maintaining their relative orders.
 */
class OneDownCommand(
	drawingView: DrawingView<*,*>,
	componentIds: Collection<Int>
) : StackingOrderCommand("edit.action.stackingOrder.oneDown.name", drawingView, componentIds), Undoable {

	private val origStackingOrderPositions = drawing.getStackingOrderPositions(componentIds)

	override fun execute() {
		var i = origStackingOrderPositions.size - 1
		for (pos in origStackingOrderPositions.asReversed()) {
			if (pos.position < drawing.drawables.size - 1) {
				val newPos = pos.position + 1
				if (i == origStackingOrderPositions.size - 1 || newPos < drawing.getStackingOrderPosition(origStackingOrderPositions[i + 1].componentId)) {
					drawing.setStackingOrderPosition(newPos, pos.componentId)
				}
			}
			i--
		}
	}

	override fun undo() {
		for (pos in origStackingOrderPositions.asReversed()) {
			drawing.setStackingOrderPosition(pos.position, pos.componentId)
		}
	}
}

/**
 * Brings a [Collection] of [Component]s to the back of the stacking order.
 */
class ToBackCommand(
	drawingView: DrawingView<*,*>,
	componentIds: Collection<Int>
) : StackingOrderCommand("edit.action.stackingOrder.toBack.name", drawingView, componentIds), Undoable {

	private val origStackingOrderPositions = drawing.getStackingOrderPositions(componentIds)
	// Maps Component ID to the position index
	private val oldPositions = mutableMapOf<Int, Int>()

	override fun execute() {
		oldPositions.clear()
		for (pos in origStackingOrderPositions) {
			oldPositions[pos.componentId] = drawing.getStackingOrderPosition(pos.componentId)
			drawing.setStackingOrderPosition(drawing.drawables.size - 1, pos.componentId)
		}
	}

	override fun undo() {
		for (pos in origStackingOrderPositions.asReversed()) {
			drawing.setStackingOrderPosition(oldPositions[pos.componentId]!!, pos.componentId)
		}
	}
}