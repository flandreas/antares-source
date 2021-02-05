package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.ui.graphpanel.GraphPanelView
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewType
import com.ccfraser.muirwik.components.*
import kotlinext.js.js
import kotlinext.js.jsObject
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface GraphPanelViewJsProps : RProps {
	var canvasId: String
	var width: Int
	var height: Int
	var drawing: GraphView
	var editor: Editor
	var applicationModeHolder: ApplicationModeHolder
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
class GraphPanelViewJs(props: GraphPanelViewJsProps) : RComponent<GraphPanelViewJsProps, RState>(props) {

	private val drawingView: DrawingView<GraphView> get() = props.editor.view as DrawingView<GraphView>

	private val treeViewController: LibraryTreeViewController =
		LibraryTreeViewController(LibraryTreeViewType.Main, LibraryModule.libraryHolder.library, null, props.applicationModeHolder)

	private val editViewController: GraphEditViewController = GraphEditViewController(drawingView)

	override fun componentDidMount() {
		editViewController.graphNavigationViewController.setRootGraphView(props.drawing, editable = true)
		props.editor.active = true
	}

	override fun componentWillUnmount() {
		editViewController.dispose()
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
				libraryTreeView {
					controller = this@GraphPanelViewJs.treeViewController
				}
			}

			styledDiv {
				css { flexGrow = 1.0 }
				graphEditView {
					canvasId = props.canvasId
					controller = this@GraphPanelViewJs.editViewController
					width = props.width
					height = props.height
				}
			}
		}
	}
}