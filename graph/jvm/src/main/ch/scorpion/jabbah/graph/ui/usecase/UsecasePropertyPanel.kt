package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.AbstractPropertyPanel
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.view.Usecase

/** An [AbstractPropertyPanel] for editing the properties of the currently selected [Usecase].*/
class UsecasePropertyPanel(
	editor: Editor,
	sheetPanelFactory: PropertySheetPanelFactory,
	eventBus: EventBus
) : AbstractPropertyPanel(editor, sheetPanelFactory) {

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
			updateEnabledness()
		}

		updateEnabledness()
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

	private fun updateEnabledness() {
		getTable().isEnabled = !(currentSavable?.readOnly ?: false)
	}
}