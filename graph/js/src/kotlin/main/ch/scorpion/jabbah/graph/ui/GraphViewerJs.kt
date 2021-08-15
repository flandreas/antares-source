package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerController
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerView
import react.*

external interface GraphViewerJsProps : RProps {
	var canvasId: String
	var metaGraphUuid: UUID
	var size: Dimension2D
}

fun RBuilder.graphViewer(handler: GraphViewerJsProps.() -> Unit): ReactElement =
	child(GraphViewerJs::class) {
		this.attrs(handler)
	}

class GraphViewerJs(
	props: GraphViewerJsProps
) : RComponent<GraphViewerJsProps, RState>(props), GraphViewerView {

	private val controller = GraphViewerController()

	init {
		controller.view = this
	}

	override fun componentDidMount() {
		val metaGraph = GraphModelModule.metaGraphRepository.getMetaGraph(props.metaGraphUuid)
		controller.setGraphView(metaGraph.graph.graphView)
	}

	override fun RBuilder.render() {
		graphNavigationView {
			canvasId = props.canvasId
			controller = this@GraphViewerJs.controller.graphNavigationViewController
			size = props.size
		}
	}

	override fun dispose() { }
}