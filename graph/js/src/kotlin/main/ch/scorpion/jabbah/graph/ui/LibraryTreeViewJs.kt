package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.mreact.jmTreeItem
import ch.scorpion.jabbah.base.mreact.jmTreeView
import com.ccfraser.muirwik.components.mIcon
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

}

fun RBuilder.libraryTreeView(handler: LibraryTreeViewJsProps.() -> Unit): ReactElement {
	return child(LibraryTreeViewJs::class) {
		this.attrs(handler)
	}
}

class LibraryTreeViewJs(
	props: LibraryTreeViewJsProps) : RComponent<LibraryTreeViewJsProps, RState>(props
) {
	override fun RBuilder.render() {
		jmTreeView {
			defaultExpandIcon = mIcon("expand_more")
			defaultCollapseIcon = mIcon("chevron_right")

			jmTreeItem {
				nodeId = "1"
				label = "Library 'Standard'"

				jmTreeItem {
					nodeId = "2"
					label = "Logical Gates"

					jmTreeItem {
						nodeId = "3"
						label = "AND"
					}
					jmTreeItem {
						nodeId = "4"
						label = "OR"
					}
				}

				jmTreeItem {
					nodeId = "5"
					label = "Input"
				}

				jmTreeItem {
					nodeId = "6"
					label = "Output"
				}
			}
		}
	}
}