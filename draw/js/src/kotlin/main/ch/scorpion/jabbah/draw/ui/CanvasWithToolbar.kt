package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.View
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

/**
 * @property size the size of the canvas in view coordinates (used for "portlet" scenarios),
 * or `null` if the canvas should adjust to the available size (used for "iframe" or "desktop" scenarios)
 */
external interface CanvasWithToolbarProps : Props {
	var canvasId: String
	var view: View<*>
	var size: Dimension2D?
	var toolbarRenderer: (RBuilder) -> Unit
}

fun RBuilder.canvasWithToolbar(handler: CanvasWithToolbarProps.() -> Unit) {
	child(CanvasWithToolbar::class) {
		this.attrs(handler)
	}
}

class CanvasWithToolbar(
	props: CanvasWithToolbarProps
) : RComponent<CanvasWithToolbarProps, State>(props) {

	override fun RBuilder.render() {
		styledDiv {
			css {
				position = Position.relative
				display = Display.flex
				flexDirection = FlexDirection.column
				flex(1.0)

			}
			if (props.size == null) {
				jCanvas {
					canvasId = props.canvasId
					view = props.view
				}
				styledDiv {
					css {
						position = Position.absolute
						top = 7.px
						left = 7.px
						width = 100.pct - 14.px
						classes.add("toolbarOverCanvas")
					}
					props.toolbarRenderer(this)
				}
			} else {
				jCanvas {
					canvasId = props.canvasId
					view = props.view
					size = props.size!!
				}
				styledDiv {
					css {
						position = Position.absolute
						top = 1.px
						left = 1.px
						width = props.size!!.width.px - 2.px
						classes.add("toolbarOverCanvas")
					}
					props.toolbarRenderer(this)
				}
			}
		}
	}
}