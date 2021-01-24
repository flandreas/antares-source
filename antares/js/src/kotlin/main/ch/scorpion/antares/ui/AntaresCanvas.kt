package ch.scorpion.antares.ui

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.DefaultSavable
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.ui.graphExecutionToolbar
import ch.scorpion.jabbah.graph.ui.graphNavigationView
import ch.scorpion.jabbah.graph.ui.graphPanelView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

external interface AntaresCanvasProps : RProps {
	var canvasId: String
	var width: Int
	var height: Int
	var drawing: GraphView
}

/** Displays simulation controls and a [graphNavigationView]. */
class AntaresCanvas(props: AntaresCanvasProps) : RComponent<AntaresCanvasProps, RState>(props) {

	private val editor: Editor
	private val applicationModeHolder: ApplicationModeHolder

	init {
		editor = GraphViewModule.graphEditorFactory.invoke(BaseModule.eventBus)
		applicationModeHolder = ApplicationModeHolderImpl(editor)
		GraphViewModule.applicationModeHolder = applicationModeHolder
	}

	override fun componentDidMount() {
		DrawViewModule.viewManager.activeView = editor.view

		ExecutionModule.scheduler.isSoftBreakpointsEnabled = true

		// In absence of a real application controller. Used to enable ToggleApplicationModeAction
		BaseModule.eventBus.post(ApplicationDataEvent(null, ApplicationData(props.drawing, DefaultSavable("Web"))))

		editor.active = true
	}

	override fun componentWillUnmount() {
		applicationModeHolder.dispose()
	}

	override fun RBuilder.render() {
		graphExecutionToolbar {
			scheduler = ExecutionModule.scheduler
			eventBus = BaseModule.eventBus
		}
		graphPanelView {
			canvasId = props.canvasId
			width = props.width
			height = props.height
			drawing = props.drawing
			editor = this@AntaresCanvas.editor
		}
	}
}