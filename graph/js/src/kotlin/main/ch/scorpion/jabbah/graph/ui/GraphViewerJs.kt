package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.module.GraphModule
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerController
import ch.scorpion.jabbah.graph.ui.graphviewer.GraphViewerView
import com.ccfraser.muirwik.components.MCircularProgressColor
import com.ccfraser.muirwik.components.mBackdrop
import com.ccfraser.muirwik.components.mCircularProgress
import com.ccfraser.muirwik.components.themeContext
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

/**
 * @property size the size of the canvas in view coordinates (used for "portlet" scenarios),
 * or `null` if the canvas should adjust to the available size (used for "iframe" or "desktop" scenarios)
 */
external interface GraphViewerJsProps : Props {
	var canvasId: String
	var metaGraphUuid: UUID
	var size: Dimension2D?
	var addMargins: Boolean?
}

external interface GraphViewerJsState : State {
	var isLoading: Boolean
}

fun RBuilder.graphViewer(handler: GraphViewerJsProps.() -> Unit) {
	child(GraphViewerJs::class) {
		this.attrs(handler)
	}
}

class GraphViewerJs(
	props: GraphViewerJsProps
) : RComponent<GraphViewerJsProps, GraphViewerJsState>(props), GraphViewerView {

	private val toolbarBackgroundColor = "#f5f5f5f0"
	private val controller = GraphViewerController()

	init {
		controller.view = this
		controller.applicationContextHolder.scheduler.isSoftBreakpointsEnabled = true
		state.isLoading = true
	}

	override fun componentDidUpdate(prevProps: GraphViewerJsProps, prevState: GraphViewerJsState, snapshot: Any) {
		if (!state.isLoading) {
			val metaGraph = GraphModule.metaGraphRepository.getMetaGraph(props.metaGraphUuid)
			controller.setMetaGraph(metaGraph)
		}
	}

	override fun RBuilder.render() {
		themeContext.Consumer { theme ->
			if (state.isLoading) {
				styledDiv {
					css {
						position = Position.relative
						width = props.size?.width?.toInt()?.px ?: 200.px
						height = props.size?.height?.toInt()?.px ?: 200.px
						if (props.addMargins == true) {
							marginLeft = 40.px
							marginBottom = 20.px
						}
					}
					mBackdrop(open = true, className = "backdrop") {
						css {
							position = Position.absolute
							height = 100.pct
							zIndex = theme.zIndex.drawer -1
							color = Color("#fff")
						}
						mCircularProgress(color = MCircularProgressColor.inherit)
					}
				}
			} else {
				graphNavigationView {
					canvasId = props.canvasId
					controller = this@GraphViewerJs.controller.graphNavigationViewController
					size = props.size
					responsive = false
					addMargins = props.addMargins
					toolbarBackgroundColor = this@GraphViewerJs.toolbarBackgroundColor
					canvasToolbarRenderer = {
						it.graphExecutionToolbar {
							currentSystemSpeedCategory = this@GraphViewerJs.controller.applicationContextHolder.currentSystemSpeedCategory
							scheduler = this@GraphViewerJs.controller.applicationContextHolder.scheduler
							eventBus = BaseModule.eventBus
							toggleApplicationModeAction = this@GraphViewerJs.controller.toggleApplicationModeAction
							singleStepModeAction = this@GraphViewerJs.controller.singleStepModeAction
							pauseOrResumeAction = this@GraphViewerJs.controller.pauseOrResumeAction
							backgroundColor = this@GraphViewerJs.toolbarBackgroundColor
						}
					}
				}
			}
		}
	}

	override fun dispose() { }

	override fun notifyAllResourcesLoaded() {
		setState { isLoading = false }
	}
}