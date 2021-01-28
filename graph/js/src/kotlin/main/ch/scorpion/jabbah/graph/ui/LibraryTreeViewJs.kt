package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.mreact.jmTreeItem
import ch.scorpion.jabbah.base.mreact.jmTreeView
import com.ccfraser.muirwik.components.mIcon
import com.ccfraser.muirwik.components.mTypography
import react.*

@JsModule("@material-ui/icons/ExpandMore")
@JsNonModule
external val expandIconModule: dynamic

private val expandIcon = expandIconModule.default

@JsModule("@material-ui/icons/ChevronRight")
@JsNonModule
external val collapseIconModule: dynamic

private val collapseIcon = collapseIconModule.default


external interface LibraryTreeViewJsProps : RProps {
	// empty so far
}

fun RBuilder.libraryTreeView(handler: LibraryTreeViewJsProps.() -> Unit): ReactElement {
	return child(LibraryTreeViewJs::class) {
		this.attrs(handler)
	}
}

class LibraryTreeViewJs(
	props: LibraryTreeViewJsProps
) : RComponent<LibraryTreeViewJsProps, RState>(props) {

	override fun RBuilder.render() {

		jmTreeView(defaultExpandIcon = createExpandIcon(), defaultCollapseIcon = createCollapseIcon()) {
			jmTreeItem(label = createLabel("Library 'Standard'"), nodeId = "1") {
				jmTreeItem(label = createLabel("Logical Gates"), nodeId = "2") {
					jmTreeItem(label = createLabel("AND"), nodeId = "3")
					jmTreeItem(label = createLabel("OR"), nodeId = "4")
				}
				jmTreeItem(label = createLabel("Input"), nodeId = "5") {
					jmTreeItem(label = createLabel("Switch"), nodeId = "6")
					jmTreeItem(label = createLabel("Input Port"), nodeId = "7")
				}
				jmTreeItem(label = createLabel("Output"), nodeId = "8") {
					jmTreeItem(label = createLabel("LED"), nodeId = "9")
					jmTreeItem(label = createLabel("Output Port"), nodeId = "10")
				}
			}
		}
	}

	private fun createExpandIcon(): ReactElement = RBuilder().mIcon("chevron_right")

	private fun createCollapseIcon(): ReactElement = RBuilder().mIcon("expand_more")

	private fun createLabel(text: String): ReactElement = RBuilder().mTypography(text)
}