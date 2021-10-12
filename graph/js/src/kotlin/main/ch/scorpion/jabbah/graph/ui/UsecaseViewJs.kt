package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewController
import ch.scorpion.jabbah.graph.view.GraphView
import react.*

external interface UsecaseViewJsProps : Props {
	var controller: UsecaseViewController
}

class UsecaseViewJs(
	props: UsecaseViewJsProps
) : RComponent<UsecaseViewJsProps, State>(props), UsecaseView {

	init {
		props.controller.view = this
	}

	override fun RBuilder.render() {
		// Not implemented so far
	}

	override var graphView: GraphView? = null

	override fun dispose() { }
}