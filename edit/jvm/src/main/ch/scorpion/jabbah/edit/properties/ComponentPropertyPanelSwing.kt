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
	sheetFactory: PropertySheetPanelFactory
) : AbstractPropertyPanelSwing(controller.editor, sheetFactory), ComponentPropertyPanel {

	init {
		controller.view = this
	}

    /** ---- [AbstractPropertyPanelSwing] */

    override fun getDescription(bean: Any): String {
        if (bean is Component) {
            return bean.type
        }
        return bean.toString()
    }

    /** ---- [ComponentPropertyPanelSwing] */

    override fun setupDefaultProperties() {
	    loadProperties(editor.drawing)
    }

    override fun loadComponentProperties(component: Component) {
        if (component.beanInfoClassName != null) {
	        loadProperties(component.propertyOwner, component.beanInfoClassName!!)
        } else {
	        loadProperties(component.propertyOwner)
        }
    }
}