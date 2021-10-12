package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.ui.canvasWithToolbar
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface GraphNavigationViewJsProps : Props {
	var canvasId: String
	var controller: GraphNavigationViewController
	var size: Dimension2D?
	var canvasToolbarRenderer: (RBuilder) -> Unit
	var addMargins: Boolean?
	var toolbarBackgroundColor: String
}

fun RBuilder.graphNavigationView(handler: GraphNavigationViewJsProps.() -> Unit) {
	child(GraphNavigationViewJs::class) {
		this.attrs(handler)
	}
}

/**
 * A React Material implementation of [GraphNavigationView].
 */
private class GraphNavigationViewJs(
	props: GraphNavigationViewJsProps
) : RComponent<GraphNavigationViewJsProps, State>(props), GraphNavigationView {

	init {
		props.controller.view = this
	}

	override fun componentWillUnmount() {
		dispose()
	}

	override fun RBuilder.render() {
		styledDiv {
			css {
				if (props.addMargins == true) {
					marginLeft = 40.px
					marginBottom = 20.px
				}
				if (props.size == null) {
					display = Display.flex
					height = 100.pct
					flexDirection = FlexDirection.column
					overflow = Overflow.hidden
				}
			}
			canvasWithToolbar {
				canvasId = props.canvasId
				view = props.controller.drawingView
				size = props.size
				toolbarRenderer = {
					it.navigationStackView {
						controller = props.controller.navigationStackViewController
						backgroundColor = props.toolbarBackgroundColor
					}
					props.canvasToolbarRenderer(it)
				}
			}
		}
	}

	override fun refresh() {
		forceUpdate()
	}

	override fun dispose() { }
}