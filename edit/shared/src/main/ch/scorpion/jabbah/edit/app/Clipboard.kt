package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.TypeMap

interface CopyPasteUtility {

	/** Cuts the specified [Component]s from the [Drawing] of the specified [DrawingView] to the system clipboard.*/
	fun cut(
		view: DrawingView<Drawing<Component>>,
		components: Collection<Component>,
		typeMap: TypeMap = IOModule.typeMap,
		commandManager: CommandManager = EditModule.commandManager
	)

	/** Copies the specified [Component]s to the system clipboard.*/
	fun copy(
		drawing: Drawing<*>,
		components: Collection<Component>,
		typeMap: TypeMap = IOModule.typeMap
	)

	/** Pastes the contents of the system clipboard into the [Drawing] of the specified [DrawingView].*/
	fun paste(
		view: DrawingView<Drawing<Component>>,
		storableCreator: StorableCreator = IOModule.storableCreator,
		typeMap: TypeMap = IOModule.typeMap,
		commandManager: CommandManager = EditModule.commandManager
	)
}

class CutAction(
	private val copyPasteUtility: CopyPasteUtility = EditModule.copyPasteUtility,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val typeMap: TypeMap = IOModule.typeMap,
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractSelectionAwareAction("edit.action.cut", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.activeView as DrawingView<Drawing<Component>>
		copyPasteUtility.cut(drawingView, drawingView.selectionManager.selection, typeMap, commandManager)
	}
}

class CopyAction(
	private val copyPasteUtility: CopyPasteUtility = EditModule.copyPasteUtility,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val typeMap: TypeMap = IOModule.typeMap
) : AbstractSelectionAwareAction("edit.action.copy", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.activeView as DrawingView<*>
		copyPasteUtility.copy(drawingView.drawing, drawingView.selectionManager.selection, typeMap)
	}
}

class PasteAction(
	private val copyPasteUtility: CopyPasteUtility = EditModule.copyPasteUtility,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val typeMap: TypeMap = IOModule.typeMap,
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractViewAction("edit.action.paste", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.activeView as DrawingView<Drawing<Component>>
		copyPasteUtility.paste(drawingView, storableCreator, typeMap, commandManager)
	}
}