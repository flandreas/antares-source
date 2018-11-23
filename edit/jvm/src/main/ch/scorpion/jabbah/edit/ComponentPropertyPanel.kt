package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import javax.swing.JPanel

/**
 * A [JPanel] for editing the properties of the currently selected [Component].
 */
class ComponentPropertyPanel(
    editor: Editor,
    sheetFactory: PropertySheetPanelFactory,
    eventBus: EventBus
) : AbstractPropertyPanel(editor, sheetFactory) {

	var editable: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				getTable().isEnabled = field
			}
		}

    init {
        eventBus.register(SelectionChangeEvent::class) { handle(it) }
    }

    /** ---- [AbstractPropertyPanel] */

    override fun getDescription(bean: Any): String {
        if (bean is Component) {
            return bean.type!!
        }
        return bean.toString()
    }

    /** ---- [ComponentPropertyPanel] */

    override fun setupDefaultProperties() {
	    loadProperties(editor.drawing)
    }

	private fun handle(event: SelectionChangeEvent) {
        if (event.view !== editor.view) {
            return
        }

        clearProperties()

        if (event.type !== SelectionChangeEvent.Type.SELECTED) {
	        loadProperties(editor.view.drawing)
        } else {
            val selection = getSelectedComponent(event)
            if (selection != null) {
	            loadComponentProperties(selection)
            }
        }
    }

    private fun getSelectedComponent(event: SelectionChangeEvent): Component? {
        if (event.components.size == 1) {
            return event.components.iterator().next()
        }
        return null
    }

    private fun loadComponentProperties(component: Component) {
        if (component.beanInfoClassName != null) {
	        loadProperties(component.propertyOwner, component.beanInfoClassName!!)
        } else {
	        loadProperties(component.propertyOwner)
        }
    }
}