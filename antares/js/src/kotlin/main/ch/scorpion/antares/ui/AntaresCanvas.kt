package ch.scorpion.antares.ui

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.mreact.jmButton
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import ch.scorpion.jabbah.graph.ui.GraphViewUI
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewExecutionController
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.MButtonVariant
import kotlinx.html.id
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.canvas
import styled.styledDiv

external interface AntaresCanvasProps : RProps {
	var canvasId: String
	var width: Int
	var height: Int
	var drawing: Drawing<Component>
}

/** Displays a [View] in a [CanvasJs]. */
class AntaresCanvas : RComponent<AntaresCanvasProps, RState>(), GraphViewUI {

	private val toggleModeAction = ToggleApplicationModeAction()
	private lateinit var editor: Editor
	private lateinit var applicationModeHolder: ApplicationModeHolder
	private lateinit var executionController: GraphViewExecutionController

	override fun componentDidMount() {
		editor = GraphViewModule.graphEditorFactory.invoke(props.canvasId, BaseModule.eventBus)
		editor.view.drawing = props.drawing

		applicationModeHolder = ApplicationModeHolderImpl(editor)
		GraphViewModule.applicationModeHolder = applicationModeHolder

		executionController = GraphViewExecutionController(
			this,
			isRoot = true,
			rootGraphProvider = { drawingView.drawing.graph!! },
			graphViewsProvider = { listOf(drawingView.drawing) }
		)
	}

	override fun RBuilder.render() {
		styledDiv {
			/*
			mButton("Simulate", color = MColor.primary, variant = MButtonVariant.contained, onClick = {
				toggleModeAction.execute(ActionEvent("Click", this@AntaresCanvas, 0, "click", 0))
			})
			*/

			jmButton(toggleModeAction, color = MColor.primary, variant = MButtonVariant.contained)
		}
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