package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.PasteInfo
import ch.scorpion.jabbah.edit.module.EditModule

expect object Clipboard {

	fun getStringContents(): String?

	fun setStringContents(contents: String)
}

class CutAction(
	private val service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.cut", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		service.cut(drawingView!!)
	}
}

class CopyAction(
	private val service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.copy", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		service.copy(drawingView!!)
	}
}

class PasteAction(
	private val service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractEditAction("edit.action.paste", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		service.paste(drawingView!! as DrawingView<Drawing<Component>>)
	}
}

/**
 * Adds the previously copied [Component]s to a [Drawing].
 * Use [CopyPasteService.paste] to retrieve a [PasteInfo] before registering this [PasteCommand] with a
 * [CommandManager]. Its [execute] method is only used for undo.
 */
class PasteCommand(
	private val drawingView: DrawingView<Drawing<Component>>,
	private val clipboardContents: String,
	private val pasteInfo: PasteInfo,
	private val service: CopyPasteService = EditModule.copyPasteService
) : AbstractCommand("edit.command.paste", null), Undoable {

	override fun execute() {
		service.paste(clipboardContents, drawingView, pasteInfo.dislocation)
	}

	override fun undo() {
		pasteInfo.components.forEach { drawingView.drawing.remove(drawingView.drawing.getWithId(it.id) as Component) }
		service.decrementPasteCount()
	}
}

