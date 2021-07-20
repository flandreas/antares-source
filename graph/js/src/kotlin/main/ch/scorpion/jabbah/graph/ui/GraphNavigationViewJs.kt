package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.draw.view.responsiveCanvas
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface GraphNavigationViewJsProps : RProps {
	var canvasId: String
	var controller: GraphNavigationViewController
	var size: Dimension2D?
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

	private var canvasJs: CanvasJs? = null

	init {
		props.controller.view = this
	}

	override fun componentDidMount() {
		canvasJs = CanvasJs(props.canvasId, props.controller.drawingView, props.size)
	}

	override fun componentWillUnmount() {
		dispose()
	}

	override fun RBuilder.render() {
		styledDiv {
			css {
				display = Display.flex
				flexDirection = FlexDirection.column
				width = 100.vw
				height = 100.vh
			}
			navigationStackView {
				controller = props.controller.navigationStackViewController
			}
			child(responsiveCanvas) {
				attrs.canvasId = props.canvasId
				attrs.canvasJsProvider = { canvasJs }
			}
		}
	}

	override fun refresh() {
		forceUpdate()
	}

	override fun dispose() {
		// empty
	}
}