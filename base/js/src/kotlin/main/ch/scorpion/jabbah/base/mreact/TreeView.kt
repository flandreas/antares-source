package ch.scorpion.jabbah.base.mreact

import com.ccfraser.muirwik.components.StyledPropsWithCommonAttributes
import com.ccfraser.muirwik.components.createStyled
import com.ccfraser.muirwik.components.setStyledPropsAndRunHandler
import react.*
import styled.StyledHandler

@JsModule("@material-ui/lab/TreeView")
@JsNonModule
private external val treeViewModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val treeViewComponent: RComponent<JMTreeViewProps, RState> = treeViewModule.default

interface JMTreeViewProps : StyledPropsWithCommonAttributes {
	var defaultExpandIcon: ReactElement?
	var defaultCollapseIcon: ReactElement?
}

fun RBuilder.jmTreeView(
	defaultExpandIcon: ReactElement,
	defaultCollapseIcon: ReactElement,
	addAsChild: Boolean = true,
	className: String? = null,
	handler: StyledHandler<JMTreeViewProps>? = null
) = createStyled(treeViewComponent, addAsChild) {
	attrs.defaultExpandIcon = defaultExpandIcon
	attrs.defaultCollapseIcon = defaultCollapseIcon
	setStyledPropsAndRunHandler(className, handler)
}
