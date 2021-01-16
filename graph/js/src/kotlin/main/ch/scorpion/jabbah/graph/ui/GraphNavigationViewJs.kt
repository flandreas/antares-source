package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewActorListener
import ch.scorpion.jabbah.graph.view.GraphViewExecutionController
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import react.dom.canvas
import kotlinx.html.id
import react.*

external interface GraphNavigationViewJsProps : RProps {
	var canvasId: String
	var drawing: GraphView
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
 * A lot of the inner objects in this class will have to be moved out if various instances
 * of this class are used. Most of these inner object are only relevant for the "root editor".
 */
private class GraphNavigationViewJs : RComponent<GraphNavigationViewJsProps, RState>(), GraphViewUI {

	private lateinit var editor: Editor
	private lateinit var applicationModeHolder: ApplicationModeHolder
	private lateinit var executionController: GraphViewExecutionController
	private lateinit var actorListener: GraphViewActorListener

	override fun componentDidMount() {
		editor = GraphViewModule.graphEditorFactory.invoke(props.canvasId, BaseModule.eventBus)
		editor.view.drawing = props.drawing as Drawing<Component>
		applicationModeHolder = ApplicationModeHolderImpl(editor)
		GraphViewModule.applicationModeHolder = applicationModeHolder

		executionController = GraphViewExecutionController(
			this,
			isRoot = true,
			rootGraphProvider = { drawingView.drawing.graph!! },
			graphViewsProvider = { listOf(drawingView.drawing) }
		)

		actorListener = GraphViewActorListener(editor.view as DrawingView<GraphView>)
	}

	override fun componentWillUnmount() {
		editor.view.dispose()
		applicationModeHolder.dispose()
		executionController.dispose()
	}

	override fun RBuilder.render() {
		canvas {
			attrs.id = props.canvasId
			attrs.width = props.width.toString()
			attrs.height = props.height.toString()
		}
	}

	override val drawingView: DrawingView<GraphView> get() = editor.view as DrawingView<GraphView>

	override val isEditable: Boolean get() = true

	override val isDetached: Boolean get() = false

	override fun deselectAll() {
		drawingView.content.selectionManager.deselectAll()
	}
}