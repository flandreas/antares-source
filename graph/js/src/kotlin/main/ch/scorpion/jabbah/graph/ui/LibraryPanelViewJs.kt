package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.library.LibraryTreeNode
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelController
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelView
import react.*

external interface LibraryPanelViewJsProps : RProps {
	var controller: LibraryPanelController
}

fun RBuilder.libraryPanelView(handler: LibraryPanelViewJsProps.() -> Unit): ReactElement {
	return child(LibraryPanelViewJs::class) {
		this.attrs(handler)
	}
}

class LibraryPanelViewJs(
	props: LibraryPanelViewJsProps
) : RComponent<LibraryPanelViewJsProps, RState>(props), LibraryPanelView {

	override fun componentDidMount() {
		props.controller.view = this
	}

	override fun dispose() { }

	override fun refresh() {
		forceUpdate()
	}

	override fun RBuilder.render() {
		libraryTreeView {
			controller = this@LibraryPanelViewJs.props.controller.libraryTreeViewController
			onDoubleClick = ::onDoubleClick
		}
	}

	private fun onDoubleClick(node: LibraryTreeNode) {
		console.info("DoubleClick on '${node.item.name}'")
	}
}