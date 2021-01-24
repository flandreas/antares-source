package ch.scorpion.jabbah.base.mreact

import com.ccfraser.muirwik.components.StyledPropsWithCommonAttributes
import com.ccfraser.muirwik.components.createStyled
import react.RBuilder
import react.RComponent
import react.RState
import react.ReactElement

@JsModule("@material-ui/lab/TreeView")
@JsNonModule
private external val treeViewModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val treeViewComponent: RComponent<JMTreeViewProps, RState> = treeViewModule.default

interface JMTreeViewProps : StyledPropsWithCommonAttributes {
	var defaultExpandIcon: ReactElement?
	var defaultCollapseIcon: ReactElement?
}

fun RBuilder.jmTreeView(handler: JMTreeViewProps.() -> Unit): ReactElement {
	return child(JabbahMaterialTreeView::class) {
		this.attrs(handler)
	}
}

class JabbahMaterialTreeView(props: JMTreeViewProps) : RComponent<JMTreeViewProps, RState>(props) {

	override fun RBuilder.render() {
		createStyled(treeViewComponent) {
			attrs.defaultExpandIcon = props.defaultExpandIcon
			attrs.defaultCollapseIcon = props.defaultCollapseIcon
		}
	}
}