package ch.scorpion.antares.model.testcase

import ch.scorpion.jabbah.edit.properties.AbstractPropertyPanelSwing
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory

class TestcasePropertyPanelSwing(
	controller: TestcasePropertyPanelController,
	sheetPanelFactory: PropertySheetPanelFactory
) : AbstractPropertyPanelSwing(controller, "testcase", sheetPanelFactory), TestcasePropertyPanel {

	init {
		controller.view = this
	}
}