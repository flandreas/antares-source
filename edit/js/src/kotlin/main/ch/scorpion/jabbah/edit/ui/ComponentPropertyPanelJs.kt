package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.edit.module.EditModuleJs
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.spacingUnits
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface ComponentPropertyPanelJsProps : Props {
	var controller: ComponentPropertyPanelController
}

fun RBuilder.componentPropertyPanel(handler: ComponentPropertyPanelJsProps.() -> Unit) {
	child(ComponentPropertyPanelJs::class) {
		this.attrs(handler)
	}
}

class ComponentPropertyPanelJs(
	props: ComponentPropertyPanelJsProps
) : RComponent<ComponentPropertyPanelJsProps, State>(props), ComponentPropertyPanel {

	override fun componentDidMount() {
		props.controller.view = this
	}

	override fun RBuilder.render() {
		styledDiv {
			css {
				paddingLeft = 2.spacingUnits
				maxHeight = LinearDimension.fillAvailable
				maxWidth = LinearDimension.fillAvailable
				flexGrow = 1.0
				overflow = Overflow.auto
			}
			mTypography(props.controller.title, MTypographyVariant.h5)
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