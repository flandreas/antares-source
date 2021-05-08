package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.SelectionChangeEvent

interface ComponentPropertyPanel : PropertyPanel {
	fun loadComponentProperties(component: Component)
}

/**
 * Displays the properties of the currently selected [Component] and allows the user to edit them.
 */
class ComponentPropertyPanelController(
	editor: Editor,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractPropertyPanelController<ComponentPropertyPanel>(editor) {

	private val selectionChangeHandler: EventHandler<SelectionChangeEvent> = { handle(it) }

	init {
		eventBus.register(SelectionChangeEvent::class, selectionChangeHandler)
	}

	/** ---- [AbstractUIController] */

	override fun dispose() {
		super.dispose()
		eventBus.unregister(selectionChangeHandler)
	}

	/** ---- [ComponentPropertyPanelController] */

	private fun handle(event: SelectionChangeEvent) {
		if (event.view !== editor.view) {
			return
		}

		view.clearProperties()

		if (event.type !== SelectionChangeEvent.Type.SELECTED) {
			view.loadProperties(editor.view.drawing)
		} else {
			getSelectedComponent(event)?.let {
				view.loadComponentProperties(it)
			}
		}
	}

	private fun getSelectedComponent(event: SelectionChangeEvent): Component? {
		if (event.components.size == 1) {
			return event.components.iterator().next()
		}
		return null
	}
}