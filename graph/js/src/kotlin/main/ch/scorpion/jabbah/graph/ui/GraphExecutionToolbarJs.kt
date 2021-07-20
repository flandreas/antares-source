package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.mreact.jmToggleButton
import ch.scorpion.jabbah.execution.PauseExecutionAction
import ch.scorpion.jabbah.execution.ResumeExecutionAction
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.systemSpeedSlider
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import com.ccfraser.muirwik.components.*
import kotlinx.css.paddingLeft
import react.*
import styled.css
import styled.styledDiv

external interface GraphExecutionToolbarJsProps : RProps {
	var applicationDataHolder: ApplicationDataHolder
	var scheduler: Scheduler
	var eventBus: EventBus
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

	private val toggleModeAction = ToggleApplicationModeAction(props.applicationDataHolder, props.eventBus)
	private val pauseAction = PauseExecutionAction(props.scheduler, props.eventBus)
	private val resumeAction = ResumeExecutionAction(props.scheduler, props.eventBus)

	override fun componentDidMount() {
		toggleModeAction.enabled = true
	}

	override fun componentWillUnmount() {
		toggleModeAction.dispose()
		pauseAction.dispose()
		resumeAction.dispose()
	}

	override fun RBuilder.render() {
		styledDiv {
			css {
				paddingLeft = 2.spacingUnits
			}
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
	}
}