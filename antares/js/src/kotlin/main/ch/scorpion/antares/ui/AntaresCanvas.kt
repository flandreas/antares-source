package ch.scorpion.antares.ui

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.DefaultSavable
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.mreact.jrToggleButton
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.execution.PauseExecutionAction
import ch.scorpion.jabbah.execution.ResumeExecutionAction
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import ch.scorpion.jabbah.graph.ui.GraphViewUI
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewActorListener
import ch.scorpion.jabbah.graph.view.GraphViewExecutionController
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
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
class AntaresCanvas(props: AntaresCanvasProps) : RComponent<AntaresCanvasProps, RState>(props), GraphViewUI {

	private val toggleModeAction = ToggleApplicationModeAction()
	private val pauseAction = PauseExecutionAction()
	private val resumeAction = ResumeExecutionAction()
	private lateinit var editor: Editor
	private lateinit var applicationModeHolder: ApplicationModeHolder
	private lateinit var executionController: GraphViewExecutionController
	private lateinit var actorListener: GraphViewActorListener

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

		actorListener = GraphViewActorListener(editor.view as DrawingView<GraphView>)

		ExecutionModule.scheduler.isSoftBreakpointsEnabled = true
		toggleModeAction.enabled = true

		// In absence of a real application controller. Used to enable ToggleApplicationModeAction
		BaseModule.eventBus.post(ApplicationDataEvent(null, ApplicationData(props.drawing, DefaultSavable("Web"))))
	}

	// TODO Unmount

	override fun RBuilder.render() {
		styledDiv {
			jrToggleButton {
				action = toggleModeAction
				iconName = "play_arrow"
			}
			jrToggleButton {
				action = pauseAction
				iconName = "pause"
			}
			jrToggleButton {
				action = resumeAction
				iconName = "skip_next"
			}
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