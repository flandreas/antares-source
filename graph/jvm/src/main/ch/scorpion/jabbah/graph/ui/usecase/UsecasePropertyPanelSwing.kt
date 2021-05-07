package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.properties.AbstractPropertyPanelSwing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.view.Usecase

/** An [AbstractPropertyPanelSwing] for editing the properties of the currently selected [Usecase].*/
class UsecasePropertyPanelSwing(
	editor: Editor,
	sheetPanelFactory: PropertySheetPanelFactory,
	eventBus: EventBus
) : AbstractPropertyPanelSwing(editor, sheetPanelFactory) {

	private var currentSavable: Savable? = null

	init {
		eventBus.register(UsecaseSelectionEvent::class) {
			clearProperties()
			if (it.usecase != null) {
				loadProperties(it.usecase)
			}
		}

		eventBus.register(CurrentSavableEvent::class) {
			this.currentSavable = it.savable
		}
	}

	override fun setupDefaultProperties() {
		// empty
	}

	override fun getDescription(bean: Any): String? {
		if (bean is Usecase) {
			return bean.name.value
		}
		return null
	}
}