package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewController
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewController
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
class GraphEditViewJs(
	props: GraphEditViewJsProps
) : RComponent<GraphEditViewJsProps, RState>(props), GraphEditView {

	// Not used so for, but needed to satisfy controllers
	private val scenarioView = ScenarioViewJs(object : ScenarioViewJsProps {
		override var controller: ScenarioViewController = props.controller.scenarioViewController
	})

	// Not used so for, but needed to satisfy controllers
	private val usecaseView = UsecaseViewJs(object : UsecaseViewJsProps {
		override var controller: UsecaseViewController = props.controller.usecaseViewController
	})

	init {
		props.controller.view = this
	}

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