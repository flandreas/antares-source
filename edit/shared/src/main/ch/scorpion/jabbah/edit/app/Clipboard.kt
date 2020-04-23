package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.model.CopyPasteService
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
		service.cut(drawingView!! as DrawingView<Drawing<Component>>)
	}
}

class CopyAction(
	private val service: DrawingAppService = EditModule.drawingAppService,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractSelectionAwareAction("edit.action.copy", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		service.copy(drawingView!! as DrawingView<Drawing<Component>>)
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

class CopyCommand(
	private val drawingView: DrawingView<Drawing<Component>>,
	private val componentIds: Collection<Int>,
	private val service: CopyPasteService = EditModule.copyPasteService
) : AbstractCommand("edit.command.copy") {

	var oldContent: String? = null

	override fun execute() {
		oldContent = Clipboard.getStringContents()
		Clipboard.setStringContents(service.copy(componentIds, drawingView.drawing))
	}

	override fun undo() {
		oldContent?.let { Clipboard.setStringContents(it) }
	}
}

/**
 * Adds the previously copied [Component]s to a [Drawing].
 */
class PasteCommand(
	private val drawingView: DrawingView<Drawing<Component>>,
	private val clipboardContents: String,
	private val service: CopyPasteService = EditModule.copyPasteService
) : AbstractCommand("edit.command.paste", null) {

	lateinit var components: Collection<Component>

	override fun execute() {
		components = service.paste(clipboardContents, drawingView)
	}

	override fun undo() {
		components.forEach { drawingView.drawing.remove(it) }
	}
}

