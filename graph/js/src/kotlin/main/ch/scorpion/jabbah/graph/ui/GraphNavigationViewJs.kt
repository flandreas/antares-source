package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.draw.view.CanvasJs
import com.ccfraser.muirwik.components.mPaper
import kotlinx.html.id
import react.*
import react.dom.canvas

external interface GraphNavigationViewJsProps : RProps {
	var canvasId: String
	var controller: GraphNavigationViewController
	var width: Int
	var height: Int
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

	override fun componentDidMount() {
		CanvasJs(props.canvasId, props.controller.drawingView)
	}

	override fun componentWillUnmount() {
		dispose()
	}

	override fun RBuilder.render() {
		mPaper {
			navigationStackView {
				controller = props.controller.navigationStackViewController
			}
			canvas {
				attrs.id = props.canvasId
				attrs.width = props.width.toString()
				attrs.height = props.height.toString()
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