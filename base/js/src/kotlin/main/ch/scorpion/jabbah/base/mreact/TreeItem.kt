package ch.scorpion.jabbah.base.mreact

import com.ccfraser.muirwik.components.StyledPropsWithCommonAttributes
import com.ccfraser.muirwik.components.createStyled
import com.ccfraser.muirwik.components.setStyledPropsAndRunHandler
import org.w3c.dom.events.MouseEvent
import react.RBuilder
import react.RComponent
import react.RState
import react.ReactElement
import styled.StyledHandler

@JsModule("@material-ui/lab/TreeItem")
@JsNonModule
private external val treeItemModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val treeItemComponent: RComponent<JMTreeItemProps, RState> = treeItemModule.default

interface JMTreeItemProps : StyledPropsWithCommonAttributes {
	var label: ReactElement
	var nodeId: String?
	var icon: ReactElement?
	var onLabelClick: ((MouseEvent) -> Unit)?
}

fun RBuilder.jmTreeItem(
	label: ReactElement,
	nodeId: String,
	icon: ReactElement? = null,
	onLabelClick: ((MouseEvent) -> Unit)? = null,
	onDoubleClick: ((MouseEvent) -> Unit)? = null,
	addAsChild: Boolean = true,
	className: String? = null,
	handler: StyledHandler<JMTreeItemProps>? = null
) = createStyled(treeItemComponent, addAsChild) {
	attrs.label = label
	attrs.nodeId = nodeId
	attrs.icon = icon
	attrs.onLabelClick = onLabelClick
	attrs.onDoubleClick = onDoubleClick
	setStyledPropsAndRunHandler(className, handler)
}
