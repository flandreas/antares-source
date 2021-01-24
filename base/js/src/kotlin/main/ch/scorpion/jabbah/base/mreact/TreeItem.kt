package ch.scorpion.jabbah.base.mreact

import com.ccfraser.muirwik.components.StyledPropsWithCommonAttributes
import com.ccfraser.muirwik.components.createStyled
import react.RBuilder
import react.RComponent
import react.RState
import react.ReactElement

@JsModule("@material-ui/lab/TreeItem")
@JsNonModule
private external val treeItemModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val treeItemComponent: RComponent<JMTreeItemProps, RState> = treeItemModule.default

interface JMTreeItemProps : StyledPropsWithCommonAttributes {
	var label: String
	var nodeId: String?
}

fun RBuilder.jmTreeItem(handler: JMTreeItemProps.() -> Unit): ReactElement {
	return child(JabbahMaterialTreeItem::class) {
		this.attrs(handler)
	}
}

class JabbahMaterialTreeItem(props: JMTreeItemProps) : RComponent<JMTreeItemProps, RState>(props) {

	override fun RBuilder.render() {
		createStyled(treeItemComponent) {
			attrs.label = props.label
			attrs.nodeId = props.nodeId
		}
	}
}