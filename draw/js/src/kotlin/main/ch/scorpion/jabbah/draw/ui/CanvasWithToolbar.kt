package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.View
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface CanvasWithToolbarProps : RProps {
	var canvasId: String
	var view: View<*>
	var size: Dimension2D
	var toolbarRenderer: (RBuilder) -> Unit
}

fun RBuilder.canvasWithToolbar(handler: CanvasWithToolbarProps.() -> Unit): ReactElement =
	child(CanvasWithToolbar::class) {
		this.attrs(handler)
	}

class CanvasWithToolbar(
	props: CanvasWithToolbarProps
) : RComponent<CanvasWithToolbarProps, RState>(props) {

	override fun RBuilder.render() {
		styledDiv {
			css {
				position = Position.relative
			}
			jCanvas {
				canvasId = props.canvasId
				view = props.view
				size = props.size
			}

			styledDiv {
				css {
					position = Position.absolute
					top = 1.px
					left = 1.px
					width = props.size.width.px - 1.px
					classes = mutableListOf("toolbarOverCanvas")
				}
				props.toolbarRenderer(this)
			}
		}
	}
}