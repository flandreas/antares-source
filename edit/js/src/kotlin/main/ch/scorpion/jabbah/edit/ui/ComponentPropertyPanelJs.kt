package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.edit.module.EditModuleJs
import com.ccfraser.muirwik.components.mTypography
import react.*
import react.dom.div
import react.dom.form

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
			mTypography(props.controller.title)
		}
		form {
			attrs {
				novalidate = true
				autoComplete = false
			}
			props.controller.bean?.let {
				EditModuleJs.propertyPageRendererRegistry.render(it, props.controller.editor, this)
			}
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