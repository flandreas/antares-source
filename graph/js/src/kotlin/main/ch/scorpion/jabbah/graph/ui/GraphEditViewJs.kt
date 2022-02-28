package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioView
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewController
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseView
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewController
import react.*

external interface GraphEditViewJsProps : Props {
	var canvasId: String
	var controller: GraphEditViewController
	var size: Dimension2D?
	var canvasToolbarRenderer: (RBuilder) -> Unit
}

fun RBuilder.graphEditView(handler: GraphEditViewJsProps.() -> Unit) {
	child(GraphEditViewJs::class) {
		this.attrs(handler)
	}
}

/**
 * A React Material implementation of [GraphEditView].
 * Contains so far only a [GraphNavigationView]. The [ScenarioView] and [UsecaseView] will come later.
 */
class GraphEditViewJs(
	props: GraphEditViewJsProps
) : RComponent<GraphEditViewJsProps, State>(props), GraphEditView {

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

	override val graphNavigationView: GraphNavigationView get() = props.controller.graphNavigationViewController.view

	override fun RBuilder.render() {
		graphNavigationView {
			canvasId = props.canvasId
			controller = props.controller.graphNavigationViewController
			size = props.size
			responsive = true
			canvasToolbarRenderer = props.canvasToolbarRenderer
		}
	}

	override fun dispose() { }
}