package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.CanvasJs
import kotlinx.browser.window
import kotlinx.css.border
import kotlinx.css.height
import kotlinx.css.px
import kotlinx.css.width
import kotlinx.html.id
import kotlinx.html.tabIndex
import react.*
import react.dom.attrs
import styled.css
import styled.styledCanvas

external interface CanvasElementProps : RProps {
	var canvasId: String
	var view: View<*>
	var size: Dimension2D
}

fun RBuilder.jCanvas(handler: CanvasElementProps.() -> Unit): ReactElement =
	child(CanvasElement::class) {
		this.attrs(handler)
	}

class CanvasElement(
	props: CanvasElementProps
) : RPureComponent<CanvasElementProps, RState>(props) {

	override fun componentDidMount() {
		val canvasJs = CanvasJs(props.canvasId, props.view, props.size)
		canvasJs.repaint()
	}

	override fun RBuilder.render() {
		styledCanvas {
			css {
				width = props.size.width.toInt().px
				height = props.size.height.toInt().px
				border = "1px solid gray"
			}
			attrs {
				id = props.canvasId
				width = "${props.size.width.toInt() * window.devicePixelRatio.toInt()}"
				height = "${props.size.height.toInt() * window.devicePixelRatio.toInt()}"
				tabIndex = "1"
			}
		}
	}
}