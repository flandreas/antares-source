package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewController
import ch.scorpion.jabbah.graph.view.GraphView
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

external interface ScenarioViewJsProps : RProps {
	var controller: ScenarioViewController
}

class ScenarioViewJs(
	props: ScenarioViewJsProps
) : RComponent<ScenarioViewJsProps, RState>(props), ScenarioView {

	init {
		props.controller.view = this
	}

	override fun RBuilder.render() {
		// Not implemented so far
	}

	override var graphView: GraphView? = null

	override fun dispose() { }
}