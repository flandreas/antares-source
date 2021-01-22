package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import react.*

external interface GraphEditViewJsProps : RProps {
	var canvasId: String
	var controller: GraphEditViewController
	var width: Int
	var height: Int
}

fun RBuilder.graphEditView(handler: GraphEditViewJsProps.() -> Unit): ReactElement {
	return child(GraphEditViewJs::class) {
		this.attrs(handler)
	}
}

/**
 * A React Material implementation of [GraphEditView].
 * Contains so far only a [GraphNavigationView]. The [ScenarioView] and [UsecaseView] will come later.
 */
private class GraphEditViewJs(
	props: GraphEditViewJsProps
) : RComponent<GraphEditViewJsProps, RState>(props), GraphEditView {

	override fun RBuilder.render() {
		graphNavigationView {
			canvasId = props.canvasId
			controller = props.controller.graphNavigationViewController
			width = props.width
			height = props.height
		}
	}

	override fun dispose() { }
}