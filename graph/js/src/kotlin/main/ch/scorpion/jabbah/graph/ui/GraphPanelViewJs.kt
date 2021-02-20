package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.execution.issue.IssuesViewController
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelViewController
import ch.scorpion.jabbah.graph.ui.logview.LogViewController
import com.ccfraser.muirwik.components.*
import kotlinext.js.js
import kotlinext.js.jsObject
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface GraphPanelViewJsProps : RProps {
	var controller: GraphPanelViewController
	var application: Application
	var canvasId: String
	var width: Int
	var height: Int
	var metaGraph: MetaGraph
}

fun RBuilder.graphPanelView(handler: GraphPanelViewJsProps.() -> Unit): ReactElement {
	return child(GraphPanelViewJs::class) {
		this.attrs(handler)
	}
}

/**
 * A preliminary version of a JavaScript [GraphPanelView] that doesn't use the
 * corresponding controller object yet, but displays a library next to a [GraphEditViewJs].
 */
class GraphPanelViewJs(
	props: GraphPanelViewJsProps
) : RComponent<GraphPanelViewJsProps, RState>(props), GraphPanelView {

	init {
		props.controller.view = this
	}

	private val graphEditView = RBuilder().apply { graphEditView {
		canvasId = props.canvasId
		controller = props.controller.editViewController
		width = props.width
		height = props.height
	} }.childList[0] as ReactElement

	// Not used so for, but needed to satisfy controllers
	private val issuesView = IssuesViewJs(object : IssuesViewJsProps {
		override var controller: IssuesViewController = props.controller.issuesViewController
	})

	// Not used so for, but needed to satisfy controllers
	private val logView = LogViewJs(object : LogViewJsProps{
		override var controller: LogViewController = props.controller.logViewController
	})

	/** ---- [GraphPanelView] */

	override var maxIssueSeverity: IssueSeverity? = null

	override fun dispose() { }

	/** ---- [RComponent] */

	override fun componentDidMount() {
		props.controller.setApplicationData(props.metaGraph.graph.graphView, editable = true)
		DrawViewModule.viewManager.activeView = props.controller.editor.view

		(props.controller.editor.view.canvas as CanvasJs).dragTargetHandler =
			GraphPanelDragTargetHandler(props.controller.editor)
	}

	override fun RBuilder.render() {
		styledDiv {
			css {
				overflow = Overflow.hidden
				position = Position.relative
				display = Display.flex
			}

			// According to "testapp" of github:cfnz/muirwik
			val pp: MPaperProps = jsObject {  }
			pp.asDynamic().style = js { position = "relative" }
			mDrawer(open = true, MDrawerAnchor.left, MDrawerVariant.permanent, paperProps = pp) {
				libraryPanelView {
					application = this@GraphPanelViewJs.props.application
					controller = this@GraphPanelViewJs.props.controller.libraryPanelController
				}
			}

			styledDiv {
				css { flexGrow = 1.0 }
				graphDesktopView {
					controller = props.controller.desktopController
					graphEditView = this@GraphPanelViewJs.graphEditView
				}
			}
		}
	}
}