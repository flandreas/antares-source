package io.antarescircuit.jabbah.graph.ui.usecase

import io.antarescircuit.jabbah.edit.properties.AbstractPropertyPanelSwing
import io.antarescircuit.jabbah.edit.properties.PropertySheetPanelFactory
import io.antarescircuit.jabbah.graph.view.Usecase

/** An [AbstractPropertyPanelSwing] for editing the properties of the currently selected [Usecase].*/
class UsecasePropertyPanelSwing(
	controller: UsecasePropertyPanelController,
	sheetPanelFactory: PropertySheetPanelFactory,
) : AbstractPropertyPanelSwing(controller, "usecase", sheetPanelFactory), UsecasePropertyPanel {

	init {
		controller.view = this
	}
}