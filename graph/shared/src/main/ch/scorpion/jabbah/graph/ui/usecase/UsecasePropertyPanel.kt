package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.ui.AbstractPropertyPanelController
import ch.scorpion.jabbah.edit.ui.PropertyPanel

interface UsecasePropertyPanel : PropertyPanel

class UsecasePropertyPanelController(
	editor: Editor,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractPropertyPanelController<UsecasePropertyPanel>(editor) {

	private val usecaseSelectionHandler: EventHandler<UsecaseSelectionEvent> = { handle(it) }

	init {
		eventBus.register(UsecaseSelectionEvent::class, usecaseSelectionHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(usecaseSelectionHandler)
	}

	private fun handle(event: UsecaseSelectionEvent) {
		view.clearProperties()
		event.usecase?.let { view.loadProperties(it) }
	}
}