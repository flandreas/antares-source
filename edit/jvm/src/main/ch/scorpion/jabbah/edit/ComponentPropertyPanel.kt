package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus

/**
 * A [JPanel] for editing the properties of the currently selected [Component].
 */
class ComponentPropertyPanel(
    editor: Editor,
    sheetFactory: PropertySheetPanelFactory,
    eventBus: EventBus
) : AbstractPropertyPanel(editor, sheetFactory) {

    init {
        eventBus.register(SelectionChangeEvent::class, {handle(it)})
    }

    /** ---- [AbstractPropertyPanel] */

    override fun getDescription(bean: Any): String {
        if (bean is Component) {
            return bean.type!!
        }
        return bean.toString()
    }

    /** ---- [ComponentPropertyPanel] */

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
        if (component.beanInfoClassName != null) {
            updateProperties(component.propertyOwner, component.beanInfoClassName!!)
        } else {
            updateProperties(component.propertyOwner)
        }
    }
}