package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractDrawingViewCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.PasteInfo
import ch.scorpion.jabbah.edit.module.EditModule

class CutAction(
	service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractDeleteAction("edit.action.cut", eventBus, viewManager, service) {

	override fun executeImpl(components: List<Component>, drawingView: DrawingView<*>) {
		if (components.isNotEmpty()) {
			service.cut(components, drawingView)
		}

		// Don't do components.size != selection.size for checking whether everything has been deleted,
		// because non-deletable (by user selection!) Components might have been deleted as a side effect
		// of deleting other Components.
		if (selection.any { drawingView.drawing.contains(it) }) {
			postUndeleteableMessage()
		}
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
		} catch (e: Throwable) {
			System.showModalMessage(
				Translations.getString("edit.action.paste.name"),
				Translations.getString("application.paste.illegal.error", e.message!!),
				isError = true
			)
		}
	}
}

/**
 * Adds the previously copied [Component]s to a [Drawing].
 * Use [CopyPasteService.paste] to retrieve a [PasteInfo] before registering this [PasteCommand] with a
 * [CommandManager]. Its [execute] method is only used for undo.
 */
class PasteCommand(
	drawingView: DrawingView<Drawing<Component>>,
	private val clipboardContents: String,
	private var pasteInfo: PasteInfo,
	private val service: CopyPasteService = EditModule.copyPasteService
) : AbstractDrawingViewCommand("edit.command.paste", drawingView), Undoable {

	override fun execute() {
		pasteInfo = service.paste(clipboardContents, view as DrawingView<Drawing<Component>>, pasteInfo.dislocation)
	}

	override fun undo() {
		pasteInfo.componentIds.forEach { view.drawing.remove(view.drawing.getWithId(it) as Component) }
		service.decrementPasteCount()
	}
}

class DuplicateAction(
	private val service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.duplicate", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		if (service.duplicate(drawingView!!) != drawingView!!.selectionManager.selection.size) {
			eventBus.post(ComponentMessage(
				ComponentMessageType.Info,
				null,
				"edit.action.duplicate.notAllDuplicated.msg")
			)
		}
	}
}

class DuplicateCommand(
	drawingView: DrawingView<Drawing<Component>>,
	private val contents: String,
	private var pasteInfo: PasteInfo,
	private val service: CopyPasteService = EditModule.copyPasteService
) : AbstractDrawingViewCommand("edit.action.duplicate.name", drawingView), Undoable {

	override fun execute() {
		pasteInfo = service.paste(contents, view as DrawingView<Drawing<Component>>, pasteInfo.dislocation)
	}

	override fun undo() {
		pasteInfo.componentIds.forEach { view.drawing.remove(view.drawing.getWithId(it) as Component) }
		service.decrementPasteCount()
	}
}

