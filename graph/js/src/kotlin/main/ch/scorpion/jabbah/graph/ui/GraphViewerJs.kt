package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
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
external interface GraphViewerJsProps : RProps {
	var canvasId: String
	var metaGraphUuid: UUID
	var size: Dimension2D?
	var addMargins: Boolean?
}

external interface GraphViewerJsState : RState {
	var isLoading: Boolean
}

fun RBuilder.graphViewer(handler: GraphViewerJsProps.() -> Unit): ReactElement =
	child(GraphViewerJs::class) {
		this.attrs(handler)
	}

class GraphViewerJs(
	props: GraphViewerJsProps
) : RComponent<GraphViewerJsProps, GraphViewerJsState>(props), GraphViewerView {

	private val controller = GraphViewerController()

	init {
		controller.view = this
		controller.applicationContextHolder.scheduler.isSoftBreakpointsEnabled = true
		state.isLoading = true
	}

	override fun componentDidUpdate(prevProps: GraphViewerJsProps, prevState: GraphViewerJsState, snapshot: Any) {
		if (!state.isLoading) {
			val metaGraph = GraphModelModule.metaGraphRepository.getMetaGraph(props.metaGraphUuid)
			controller.setGraphView(metaGraph.graph.graphView)
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
					addMargins = props.addMargins
					canvasToolbarRenderer = {
						it.graphExecutionToolbar {
							currentSystemSpeedCategory = this@GraphViewerJs.controller.applicationContextHolder.currentSystemSpeedCategory
							scheduler = this@GraphViewerJs.controller.applicationContextHolder.scheduler
							eventBus = BaseModule.eventBus
							toggleApplicationModeAction = this@GraphViewerJs.controller.toggleApplicationModeAction
							pauseAction = this@GraphViewerJs.controller.pauseAction
							resumeAction = this@GraphViewerJs.controller.resumeAction
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