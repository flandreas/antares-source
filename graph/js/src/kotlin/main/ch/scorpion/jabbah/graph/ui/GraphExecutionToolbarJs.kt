package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.mreact.jmToggleButton
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.systemSpeedSlider
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import com.ccfraser.muirwik.components.MGridAlignItems
import com.ccfraser.muirwik.components.MGridSize
import com.ccfraser.muirwik.components.mGridContainer
import com.ccfraser.muirwik.components.mGridItem
import kotlinx.css.background
import react.*
import styled.css
import styled.styledDiv

external interface GraphExecutionToolbarJsProps : RProps {
	var currentSystemSpeedCategory: CurrentSystemSpeedCategory
	var scheduler: Scheduler
	var eventBus: EventBus
	var toggleApplicationModeAction: ToggleApplicationModeAction
	var pauseAction: Action
	var resumeAction: Action
	var backgroundColor: String
}

fun RBuilder.graphExecutionToolbar(handler: GraphExecutionToolbarJsProps.() -> Unit): ReactElement {
	return child(GraphExecutionToolbarJs::class) {
		this.attrs(handler)
	}
}

/**
 * Displays controls for switching between editing and simulation and for controlling
 * simulation execution.
 */
class GraphExecutionToolbarJs(
	props: GraphExecutionToolbarJsProps
) : RComponent<GraphExecutionToolbarJsProps, RState>(props) {

	override fun componentDidMount() {
		props.toggleApplicationModeAction.enabled = true
	}

	override fun RBuilder.render() {
		styledDiv {
			css {
				background = props.backgroundColor
			}
			mGridContainer(alignItems = MGridAlignItems.center) {
				mGridItem {
					jmToggleButton {
						action = props.toggleApplicationModeAction
						iconName = "play_arrow"
					}
					jmToggleButton {
						action = props.pauseAction
						iconName = "pause"
					}
					jmToggleButton {
						action = props.resumeAction
						iconName = "skip_next"
					}
				}
				mGridItem(xs = MGridSize.cells3) {
					systemSpeedSlider(props.currentSystemSpeedCategory) { }
				}
			}
		}
	}
}