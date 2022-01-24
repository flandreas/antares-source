package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPanelController
import javax.swing.JPanel

/**
 * A [JPanel] for editing the properties of the currently selected [Component].
 */
class ComponentPropertyPanelSwing(
	controller: ComponentPropertyPanelController,
	scope: String,
	sheetFactory: PropertySheetPanelFactory
) : AbstractPropertyPanelSwing(controller, scope, sheetFactory), ComponentPropertyPanel {

	init {
		controller.view = this
	}

    /** ---- [ComponentPropertyPanelSwing] */

    override fun setupDefaultProperties() {
	    loadProperties(controller.editor.drawing)
    }

	override fun handleBeanReplaced() {
		clearProperties()

		controller.bean?.let {
			if (it is Component) {
				loadComponentProperties(it)
			} else {
				loadProperties(it)
			}
		}
	}

	private fun loadComponentProperties(component: Component) {
		if (component.beanInfoClassName != null) {
			loadProperties(component.propertyOwner, component.beanInfoClassName!!)
		} else {
			loadProperties(component.propertyOwner)
		}
	}
}