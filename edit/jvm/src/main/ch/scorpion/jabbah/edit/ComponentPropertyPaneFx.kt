package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import javafx.scene.layout.Pane

/** A [Pane] for editing the properties of a bean.*/
class ComponentPropertyPaneFx(
	editor: Editor,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractPropertyPaneFx(editor) {

	init {
		eventBus.register(SelectionChangeEvent::class, { handle(it) })
	}

	/** ---- [AbstractPropertyPaneFx] */

	override fun getDescription(bean: Any): String? {
		if (bean is Component) {
			return bean.type
		}
		return bean.toString()
	}

	/** ---- [ComponentPropertyPaneFx] */

	private fun handle(event: SelectionChangeEvent) {
		if (event.view !== editor.view) {
			return
		}

		clearProperties()

		if (event.type !== SelectionChangeEvent.Type.SELECTED) {
			updateProperties(editor.view.drawing)
		} else {
			val selection = getSelectedComponent(event)
			if (selection != null) {
				updateComponentProperties(selection)
			}
		}
	}

	private fun getSelectedComponent(event: SelectionChangeEvent): Component? {
		if (event.components.size == 1) {
			return event.components.iterator().next()
		}
		return null
	}

	private fun updateComponentProperties(component: Component) {
		updateProperties(component.propertyOwner)
	}
}