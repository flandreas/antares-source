package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.PasteInfo
import ch.scorpion.jabbah.edit.module.EditModule

class CutAction(
	private val service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.cut", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		service.cut(drawingView!!)
	}
}

class CopyAction(
	private val service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.copy", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		service.copy(drawingView!!)
	}
}

class PasteAction(
	private val service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractEditAction("edit.action.paste", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		try {
			service.paste(drawingView!!)
		} catch (e: IllegalArgumentException) {
			eventBus.post(ComponentMessage(ComponentMessageType.Error, null, "application.paste.illegal.error"))
		}
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
	private var pasteInfo: PasteInfo,
	private val service: CopyPasteService = EditModule.copyPasteService
) : AbstractCommand("edit.command.paste", null), Undoable {

	override fun execute() {
		pasteInfo = PasteInfo(
			service.paste(clipboardContents, drawingView, pasteInfo.dislocation).map { it.id },
			pasteInfo.dislocation)
	}

	override fun undo() {
		pasteInfo.componentIds.forEach { drawingView.drawing.remove(drawingView.drawing.getWithId(it) as Component) }
		service.decrementPasteCount()
	}
}

class DuplicateAction(
	private val service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.duplicate", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		service.duplicate(drawingView!!)
	}
}

class DuplicateCommand(
	private val drawingView: DrawingView<Drawing<Component>>,
	private val contents: String,
	private var pasteInfo: PasteInfo,
	private val service: CopyPasteService = EditModule.copyPasteService
) : AbstractCommand("edit.action.duplicate.name", null), Undoable {

	override fun execute() {
		pasteInfo = PasteInfo(
			service.paste(contents, drawingView, pasteInfo.dislocation).map { it.id },
			pasteInfo.dislocation)
	}

	override fun undo() {
		pasteInfo.componentIds.forEach { drawingView.drawing.remove(drawingView.drawing.getWithId(it) as Component) }
		service.decrementPasteCount()
	}
}

