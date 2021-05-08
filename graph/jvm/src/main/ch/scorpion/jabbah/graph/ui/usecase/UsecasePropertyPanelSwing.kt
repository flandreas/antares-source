package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.edit.properties.AbstractPropertyPanelSwing
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.view.Usecase

/** An [AbstractPropertyPanelSwing] for editing the properties of the currently selected [Usecase].*/
class UsecasePropertyPanelSwing(
	controller: UsecasePropertyPanelController,
	sheetPanelFactory: PropertySheetPanelFactory,
) : AbstractPropertyPanelSwing(controller.editor, sheetPanelFactory), UsecasePropertyPanel {

	init {
		controller.view = this
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