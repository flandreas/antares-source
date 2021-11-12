package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.CanvasJs
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.*
import kotlinx.html.id
import kotlinx.html.tabIndex
import org.w3c.dom.HTMLCanvasElement
import react.*
import react.dom.attrs
import styled.css
import styled.styledCanvas

external interface CanvasElementProps : Props {
	var canvasId: String
	var view: View<*>
	var size: Dimension2D?
	var ref: RefObject<*>?
}

fun RBuilder.jCanvas(handler: CanvasElementProps.() -> Unit) {
	child(CanvasElement::class) {
		this.attrs(handler)
	}
}

class CanvasElement(
	props: CanvasElementProps
) : RPureComponent<CanvasElementProps, State>(props) {

	override fun componentDidMount() {
		val canvasElement = document.getElementById(props.canvasId) as HTMLCanvasElement

		val width = props.size?.width?.toInt() ?: canvasElement.offsetWidth
		val height = props.size?.height?.toInt() ?: canvasElement.offsetHeight

		canvasElement.width = width * window.devicePixelRatio.toInt()
		canvasElement.height = height * window.devicePixelRatio.toInt()

		val canvasJs = CanvasJs(props.canvasId, props.view, props.size)
		canvasJs.repaint()
	}

	override fun RBuilder.render() {
		if (props.size == null) {
			styledCanvas {
				css {
					flex(1.0)
					width = 100.pct - 12.px
					margin(6.px)
					border = "1px solid gray"
				}
				attrs {
					id = props.canvasId
					tabIndex = "1"
					if (props.ref != null) {
						ref = props.ref!!
					}
				}
			}
		} else {
			styledCanvas {
				css {
					width = props.size!!.width.toInt().px
					height = props.size!!.height.toInt().px
					border = "1px solid gray"
				}
				attrs {
					id = props.canvasId
					tabIndex = "1"
					if (props.ref != null) {
						ref = props.ref!!
					}
				}
			}
		}
	}
}