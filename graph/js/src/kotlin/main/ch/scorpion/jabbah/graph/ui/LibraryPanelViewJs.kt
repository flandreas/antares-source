package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryTreeNode
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelController
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelView
import react.*

external interface LibraryPanelViewJsProps : Props {
	var application: Application
	var controller: LibraryPanelController
}

fun RBuilder.libraryPanelView(handler: LibraryPanelViewJsProps.() -> Unit) {
	child(LibraryPanelViewJs::class) {
		this.attrs(handler)
	}
}

class LibraryPanelViewJs(
	props: LibraryPanelViewJsProps
) : RComponent<LibraryPanelViewJsProps, State>(props), LibraryPanelView {

	override fun componentDidMount() {
		props.controller.view = this
	}

	override fun dispose() { }

	override fun refresh() {
		forceUpdate()
	}

	override fun RBuilder.render() {
		libraryTreeView {
			application = this@LibraryPanelViewJs.props.application
			controller = this@LibraryPanelViewJs.props.controller.libraryTreeViewController
			onDoubleClick = ::onDoubleClick
		}
	}

	private fun onDoubleClick(node: LibraryTreeNode) {
		if (node.item is ContainerLibraryElement) {
			console.info("DoubleClick on '${node.item.name}'")
			props.controller.requestOpenSelectedContainerLibraryElement()
		}
	}
}