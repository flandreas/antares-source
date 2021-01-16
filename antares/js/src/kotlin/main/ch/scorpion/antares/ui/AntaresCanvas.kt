package ch.scorpion.antares.ui

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.DefaultSavable
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.mreact.jmToggleButton
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.execution.PauseExecutionAction
import ch.scorpion.jabbah.execution.ResumeExecutionAction
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.systemSpeedSlider
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import ch.scorpion.jabbah.graph.ui.graphNavigationView
import ch.scorpion.jabbah.graph.view.GraphView
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
	var drawing: Drawing<Component>
}

/** Displays simulation controls and a [graphNavigationView]. */
class AntaresCanvas(props: AntaresCanvasProps) : RComponent<AntaresCanvasProps, RState>(props) {

	private val toggleModeAction = ToggleApplicationModeAction()
	private val pauseAction = PauseExecutionAction()
	private val resumeAction = ResumeExecutionAction()

	override fun componentDidMount() {
		ExecutionModule.scheduler.isSoftBreakpointsEnabled = true
		toggleModeAction.enabled = true

		// In absence of a real application controller. Used to enable ToggleApplicationModeAction
		BaseModule.eventBus.post(ApplicationDataEvent(null, ApplicationData(props.drawing, DefaultSavable("Web"))))
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
				mGridItem(xs = MGridSize.cells1) {
					systemSpeedSlider {  }
				}
			}
		}
		graphNavigationView {
			canvasId = props.canvasId
			drawing = props.drawing as GraphView
			width = props.width
			height = props.height
		}
	}
}