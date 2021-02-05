package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.mreact.jmTreeItem
import ch.scorpion.jabbah.base.mreact.jmTreeView
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.library.LibraryDirectoryTreeModelBuilder
import ch.scorpion.jabbah.graph.library.LibraryTreeNode
import com.ccfraser.muirwik.components.mIcon
import com.ccfraser.muirwik.components.mTypography
import org.w3c.dom.events.MouseEvent
import react.*

external interface LibraryTreeViewJsProps : RProps {
	var controller: LibraryTreeViewController
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
		val model = LibraryDirectoryTreeModelBuilder(props.controller.library).build()

		jmTreeView(
			defaultExpandIcon = createExpandIcon(),
			defaultCollapseIcon = createCollapseIcon(),
		) {
			addItems(model)
		}
	}

	override fun componentDidMount() {
		props.controller.selectedItem = null
	}

	private fun nextNodeId(): String = (nodeId++).toString()

	private fun RBuilder.addItems(node: LibraryTreeNode) {
		jmTreeItem(
			label = createLabel(node.item.name.value),
			nodeId = nextNodeId(),
			onLabelClick = { event -> onLabelClick(event, node) }
		) {
			for (node in node.children) {
				addItems(node)
			}
		}
	}

	private fun createExpandIcon(): ReactElement = RBuilder().mIcon("chevron_right")

	private fun createCollapseIcon(): ReactElement = RBuilder().mIcon("expand_more")

	private fun createLabel(text: String): ReactElement = RBuilder().mTypography(text)

	private fun onLabelClick(event: MouseEvent, node: LibraryTreeNode) {
		props.controller.selectedItem = node.item
		if (node.item !is LibraryDirectory) {
			event.preventDefault()
		}
	}
}