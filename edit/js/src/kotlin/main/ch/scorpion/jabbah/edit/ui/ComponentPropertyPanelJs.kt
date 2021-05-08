package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.edit.Component
import com.ccfraser.muirwik.components.mTypography
import react.*
import react.dom.p

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
		p {
			mTypography("Component properties")
		}
	}

	/** ---- [ComponentPropertyPanel] interface */

	override fun dispose() {
		// TODO
	}

	override fun loadComponentProperties(component: Component) {
		// TODO
	}

	override fun clearProperties() {
		// TODO
	}

	override fun loadProperties(bean: Any) {
		// TODO
	}
}