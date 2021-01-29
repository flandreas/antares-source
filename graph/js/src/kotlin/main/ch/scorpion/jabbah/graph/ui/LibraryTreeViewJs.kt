package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.mreact.jmTreeItem
import ch.scorpion.jabbah.base.mreact.jmTreeView
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryDirectoryTreeModelBuilder
import ch.scorpion.jabbah.graph.library.LibraryDirectoryTreeNode
import com.ccfraser.muirwik.components.mIcon
import com.ccfraser.muirwik.components.mTypography
import react.*

external interface LibraryTreeViewJsProps : RProps {
	var library: Library
}

fun RBuilder.libraryTreeView(handler: LibraryTreeViewJsProps.() -> Unit): ReactElement {
	return child(LibraryTreeViewJs::class) {
		this.attrs(handler)
	}
}

class LibraryTreeViewJs(
	props: LibraryTreeViewJsProps
) : RComponent<LibraryTreeViewJsProps, RState>(props) {

	private var nodeId: Int = 0

	override fun RBuilder.render() {
		nodeId = 0
		val model = LibraryDirectoryTreeModelBuilder(props.library).build()

		jmTreeView(defaultExpandIcon = createExpandIcon(), defaultCollapseIcon = createCollapseIcon()) {
			addItems(model)
		}
	}

	private fun nextNodeId(): String = (nodeId++).toString()

	private fun RBuilder.addItems(parent: LibraryDirectoryTreeNode) {
		jmTreeItem(createLabel(parent.item.name.value), nextNodeId()) {
			for (node in parent.children) {
				addItems(node)
			}
		}
	}

	private fun createExpandIcon(): ReactElement = RBuilder().mIcon("chevron_right")

	private fun createCollapseIcon(): ReactElement = RBuilder().mIcon("expand_more")

	private fun createLabel(text: String): ReactElement = RBuilder().mTypography(text)
}