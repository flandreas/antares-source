package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.View
import kotlinx.css.*
import org.w3c.dom.events.Event
import react.*
import styled.css
import styled.styledDiv

external interface CanvasWithToolbarProps : RProps {
	var canvasId: String
	var view: View<*>
	var size: Dimension2D
	var toolbarRenderer: (RBuilder) -> Unit
}

external interface CanvasWithToolbarState : RState {
	var toolbarVisible: Boolean
}

fun RBuilder.canvasWithToolbar(handler: CanvasWithToolbarProps.() -> Unit): ReactElement =
	child(CanvasWithToolbar::class) {
		this.attrs(handler)
	}

class CanvasWithToolbar(
	props: CanvasWithToolbarProps
) : RComponent<CanvasWithToolbarProps, CanvasWithToolbarState>(props) {

	override fun RBuilder.render() {
		styledDiv {
			if (state.toolbarVisible) {
				styledDiv {
					css {
						position = Position.absolute
						width = props.size.width.px
					}
					props.toolbarRenderer(this)
				}
			}
			jCanvas {
				canvasId = props.canvasId
				view = props.view
				size = props.size
				mouseOverCallback = ::onMouseOver
				mouseOutCallback = ::onMouseOut
			}
		}
	}

	private fun onMouseOver(event: Event) {
		setState { toolbarVisible = true }
	}

	private fun onMouseOut(event: Event) {
		setState { toolbarVisible = false }
	}
}