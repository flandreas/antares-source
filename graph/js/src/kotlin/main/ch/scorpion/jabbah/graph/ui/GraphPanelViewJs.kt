package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.mreact.splitPane
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.ui.componentPropertyPanel
import ch.scorpion.jabbah.execution.issue.IssuesViewController
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewController
import ch.scorpion.jabbah.graph.ui.graphpanel.IssuesSummary
import ch.scorpion.jabbah.graph.ui.logview.LogViewController
import react.*

external interface GraphPanelViewJsProps : Props {
	var controller: GraphPanelViewController
	var application: Application
	var canvasId: String
	var size: Dimension2D?
	var metaGraph: MetaGraph?
}

fun RBuilder.graphPanelView(handler: GraphPanelViewJsProps.() -> Unit) {
	child(GraphPanelViewJs::class) {
		this.attrs(handler)
	}
}

/**
 * A preliminary version of a JavaScript [GraphPanelView] that doesn't use the
 * corresponding controller object yet, but displays a library next to a [GraphEditViewJs].
 */
class GraphPanelViewJs(
	props: GraphPanelViewJsProps
) : RComponent<GraphPanelViewJsProps, State>(props), GraphPanelView {

	init {
		props.controller.view = this
	}

	private val graphEditView = RBuilder().apply { graphEditView {
		canvasId = props.canvasId
		controller = props.controller.editViewController
		size = props.size
		canvasToolbarRenderer = {
			it.graphExecutionToolbar {
				currentSystemSpeedCategory = props.controller.applicationContextHolder.currentSystemSpeedCategory
				scheduler = props.controller.applicationContextHolder.scheduler
				eventBus =  BaseModule.eventBus
				toggleApplicationModeAction = props.controller.toggleApplicationModeAction
				singleStepModeAction = props.controller.singleStepModeAction
				pauseOrResumeAction = props.controller.pauseOrResumeAction
				backgroundColor = null
			}
		}
	} }.childList[0] as ReactElement

	// Not used so for, but needed to satisfy controllers
	private val issuesView = IssuesViewJs(object : IssuesViewJsProps {
		override var controller: IssuesViewController = props.controller.issuesViewController
	})

	// Not used so for, but needed to satisfy controllers
	private val logView = LogViewJs(object : LogViewJsProps{
		override var controller: LogViewController = props.controller.logViewController
	})

	override var issuesSummary: IssuesSummary? = null

	override fun dispose() { }

	/** ---- [RComponent] */

	override fun componentDidMount() {
		if (props.metaGraph != null) {
			props.controller.setApplicationData(props.metaGraph!!.graph.graphView, editable = true)
		}
		DrawViewModule.viewManager.activeView = props.controller.editor.view

		(props.controller.editor.view.canvas as CanvasJs).dragTargetHandler =
			GraphPanelDragTargetHandler(props.controller.editor)
	}

	override fun RBuilder.render() {
		splitPane(split = "vertical", defaultSize = 350, minSize = 200) {
			splitPane(split = "horizontal", defaultSize = 400, minSize = 200) {
				libraryPanelView {
					application = this@GraphPanelViewJs.props.application
					controller = this@GraphPanelViewJs.props.controller.libraryPanelController
				}
				componentPropertyPanel {
					controller = this@GraphPanelViewJs.props.controller.propertyPanelController
				}
			}
			graphDesktopView {
				controller = props.controller.desktopController
				graphEditView = this@GraphPanelViewJs.graphEditView
			}
		}
	}
}