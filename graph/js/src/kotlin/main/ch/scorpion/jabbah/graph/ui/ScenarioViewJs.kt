package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewController
import ch.scorpion.jabbah.graph.view.GraphView
import react.*

external interface ScenarioViewJsProps : Props {
	var controller: ScenarioViewController
}

class ScenarioViewJs(
	props: ScenarioViewJsProps
) : RComponent<ScenarioViewJsProps, State>(props), ScenarioView {

	init {
		props.controller.view = this
	}

	override fun RBuilder.render() {
		// Not implemented so far
	}

	override var graphView: GraphView? = null

	override fun dispose() { }
}