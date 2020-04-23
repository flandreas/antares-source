package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.app.AbstractEditAction
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.TypeMap

/**
 * An [Action] for pasting [Component]s from the clipboard into the [Drawing] of the current [DrawingView].
 */
class PasteAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val typeMap: TypeMap = IOModule.typeMap,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val cmdManager: CommandManager = EditModule.commandManager
) : AbstractEditAction("edit.action.paste", eventBus, viewManager) {

	init {
		updateEnabled()
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val drawingView = viewManager.activeView as DrawingView<Drawing<Component>>
		CopyPasteUtilitySwing.paste(drawingView, storableCreator, typeMap, cmdManager)
	}
}