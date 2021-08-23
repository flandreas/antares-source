package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.ui.canvasWithToolbar
import kotlinx.css.marginBottom
import kotlinx.css.px
import react.*
import styled.css
import styled.styledDiv

external interface GraphNavigationViewJsProps : RProps {
	var canvasId: String
	var controller: GraphNavigationViewController
	var size: Dimension2D?
	var canvasToolbarRenderer: (RBuilder) -> Unit
}

fun RBuilder.graphNavigationView(handler: GraphNavigationViewJsProps.() -> Unit): ReactElement {
	return child(GraphNavigationViewJs::class) {
		this.attrs(handler)
	}
}

/**
 * A React Material implementation of [GraphNavigationView].
 */
private class GraphNavigationViewJs(
	props: GraphNavigationViewJsProps
) : RComponent<GraphNavigationViewJsProps, RState>(props), GraphNavigationView {

	init {
		props.controller.view = this
	}

	override fun componentWillUnmount() {
		dispose()
	}

	override fun RBuilder.render() {
		styledDiv {
			css {
				marginBottom = 20.px
			}
			navigationStackView {
				controller = props.controller.navigationStackViewController
			}
			canvasWithToolbar {
				canvasId = props.canvasId
				view = props.controller.drawingView
				size = props.size!!
				toolbarRenderer = props.canvasToolbarRenderer
			}
		}
	}

	override fun refresh() {
		forceUpdate()
	}

	override fun dispose() { }
}