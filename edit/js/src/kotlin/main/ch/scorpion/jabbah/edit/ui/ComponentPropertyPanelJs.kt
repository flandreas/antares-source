package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.edit.module.EditModuleJs
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.spacingUnits
import kotlinx.css.*
import react.*
import react.dom.div
import styled.css
import styled.styledForm

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
		styledForm {
			css { display = Display.flex; flexWrap = FlexWrap.wrap; paddingBottom = 4.spacingUnits }
			/*
			attrs {
				novalidate = true
				autoComplete = false
			}
			*/
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