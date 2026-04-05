package io.antarescircuit.jabbah.graph.ui.usecase

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.properties.AbstractPropertyPanelController
import io.antarescircuit.jabbah.edit.properties.PropertyPanel
import io.antarescircuit.jabbah.graph.view.Usecase

interface UsecasePropertyPanel : PropertyPanel

class UsecasePropertyPanelController(
	editor: Editor,
	eventBus: EventBus = BaseModule.eventBus
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