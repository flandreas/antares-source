package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ContextActionRequest
import ch.scorpion.jabbah.edit.SelectionChangeEvent
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import javax.swing.JPopupMenu

/**
 * Handles [ContextActionRequest]s by showing a popup menu with [Action]s suitable
 * for the currently selected [Component]s.
 *
 * As a first version, this implementation only responds if a single [Component] is selected.
 * Later versions might build an intersection of the [Action]s of multiple selected [Component]s.
 */
class ContextActionRequestHandlerJvm(
	eventBus: EventBus = BaseModule.eventBus
) {
	companion object {
		private val LOG by logger(ContextActionRequestHandlerJvm::class)
	}

	private val popupMenu = JPopupMenu()
	private var contextMenuProvider: ContextMenuProvider? = null

	init {
		eventBus.register(ContextActionRequest::class) { handle(it) }
		eventBus.register(SelectionChangeEvent::class, { handle(it) })
	}

	private fun handle(event: ContextActionRequest) {
		LOG.debug("ContextActionRequestHandlerJvm: display popup menu")
		val selection = event.editor.view.selectionManager.selection
		updatePopupMenu(selection, event.editor.view)
	}

	private fun handle(event: SelectionChangeEvent) {
		updatePopupMenu(if (event.selected) event.components else emptyList(), event.view)
	}

	private fun updatePopupMenu(selection: Collection<Component>, view: View<*>) {
		LOG.debug(">>> ContextActionRequestHandlerJvm: fill popup menu")
		getContextMenuProvider().fillContextMenu(selection, popupMenu)
		(view.canvas as CanvasJvm).componentPopupMenu = popupMenu
	}

	private fun getContextMenuProvider(): ContextMenuProvider {
		if (contextMenuProvider == null) {
			contextMenuProvider = EditModuleJvm.contextMenuProvider.invoke()
		}
		return  contextMenuProvider!!
	}
}