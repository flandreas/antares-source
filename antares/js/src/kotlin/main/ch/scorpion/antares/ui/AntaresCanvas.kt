package ch.scorpion.antares.ui

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.DefaultSavable
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.mreact.jmToggleButton
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.execution.PauseExecutionAction
import ch.scorpion.jabbah.execution.ResumeExecutionAction
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.systemSpeedSlider
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewController
import ch.scorpion.jabbah.graph.ui.graphNavigationView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import com.ccfraser.muirwik.components.MGridAlignItems
import com.ccfraser.muirwik.components.MGridSize
import com.ccfraser.muirwik.components.mGridContainer
import com.ccfraser.muirwik.components.mGridItem
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.styledDiv

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
	private val controller: GraphNavigationViewController

	private val toggleModeAction = ToggleApplicationModeAction()
	private val pauseAction = PauseExecutionAction()
	private val resumeAction = ResumeExecutionAction()

	private val drawingView: DrawingView<GraphView> get() = editor.view as DrawingView<GraphView>

	init {
		editor = GraphViewModule.graphEditorFactory.invoke(BaseModule.eventBus)
		applicationModeHolder = ApplicationModeHolderImpl(editor)
		GraphViewModule.applicationModeHolder = applicationModeHolder

		controller = GraphNavigationViewController(isRoot = true, drawingView)
	}

	override fun componentDidMount() {
		DrawViewModule.viewManager.activeView = editor.view
		controller.setRootGraphView(props.drawing, editable = true)

		ExecutionModule.scheduler.isSoftBreakpointsEnabled = true
		toggleModeAction.enabled = true

		// In absence of a real application controller. Used to enable ToggleApplicationModeAction
		BaseModule.eventBus.post(ApplicationDataEvent(null, ApplicationData(props.drawing, DefaultSavable("Web"))))

		editor.active = true
	}

	// TODO Unmount

	override fun RBuilder.render() {
		styledDiv {
			mGridContainer(alignItems = MGridAlignItems.center) {
				mGridItem {
					jmToggleButton {
						action = toggleModeAction
						iconName = "play_arrow"
					}
					jmToggleButton {
						action = pauseAction
						iconName = "pause"
					}
					jmToggleButton {
						action = resumeAction
						iconName = "skip_next"
					}
				}
				mGridItem(xs = MGridSize.cells2) {
					systemSpeedSlider {  }
				}
			}
		}
		graphNavigationView {
			canvasId = props.canvasId
			controller = this@AntaresCanvas.controller
			width = props.width
			height = props.height
		}
	}
}