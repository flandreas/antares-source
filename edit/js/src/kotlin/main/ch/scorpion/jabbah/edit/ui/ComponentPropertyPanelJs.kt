package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.edit.module.EditModuleJs
import react.*
import react.dom.div
import react.dom.h3

external interface ComponentPropertyPanelJsProps : RProps {
	var controller: ComponentPropertyPanelController
}

fun RBuilder.componentPropertyPanel(handler: ComponentPropertyPanelJsProps.() -> Unit): ReactElement {
	return child(ComponentPropertyPanelJs::class) {
		this.attrs(handler)
	}
}

class ComponentPropertyPanelJs(
	props: ComponentPropertyPanelJsProps
) : RComponent<ComponentPropertyPanelJsProps, RState>(props), ComponentPropertyPanel {

	override fun componentDidMount() {
		props.controller.view = this
	}

	override fun RBuilder.render() {
		div {
			h3 {
				+props.controller.title
			}
		}
		props.controller.bean?.let {
			EditModuleJs.propertyPageRendererRegistry.render(it, props.controller.editor, this)
		}
	}

	/** ---- [ComponentPropertyPanel] interface */

	override fun dispose() {
		// TODO
	}

	override fun handleBeanReplaced() {
		forceUpdate()
	}
}