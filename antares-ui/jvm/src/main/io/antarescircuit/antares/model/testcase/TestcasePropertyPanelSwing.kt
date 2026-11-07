package io.antarescircuit.antares.model.testcase

import io.antarescircuit.jabbah.edit.properties.AbstractPropertyPanelSwing
import io.antarescircuit.jabbah.edit.properties.PropertySheetPanelFactory

class TestcasePropertyPanelSwing(
	controller: TestcasePropertyPanelController,
	sheetPanelFactory: PropertySheetPanelFactory
) : AbstractPropertyPanelSwing(controller, "testcase", sheetPanelFactory), TestcasePropertyPanel {

	init {
		controller.view = this
	}
}