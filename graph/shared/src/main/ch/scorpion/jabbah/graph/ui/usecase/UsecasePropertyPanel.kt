package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.AbstractPropertyPanelController
import ch.scorpion.jabbah.edit.properties.PropertyPanel
import ch.scorpion.jabbah.graph.view.Usecase

interface UsecasePropertyPanel : PropertyPanel

class UsecasePropertyPanelController(
	editor: Editor,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractPropertyPanelController<UsecasePropertyPanel>(editor) {

	private val usecaseSelectionHandler: EventHandler<UsecaseSelectionEvent> = { bean = it.usecase }

	init {
		eventBus.register(UsecaseSelectionEvent::class, usecaseSelectionHandler)
	}

	override val description: String?
		get() = when (bean) {
			is Usecase -> (bean as Usecase).name.value
			else -> null
		}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(usecaseSelectionHandler)
	}
}