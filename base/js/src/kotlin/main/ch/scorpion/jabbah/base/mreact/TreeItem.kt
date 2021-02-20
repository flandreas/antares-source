package ch.scorpion.jabbah.base.mreact

import ch.scorpion.jabbah.base.event.DragEventHandler
import ch.scorpion.jabbah.base.event.MouseEventHandler
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
	onLabelClick: MouseEventHandler? = null,
	onDoubleClick: MouseEventHandler? = null,
	onDragStart: DragEventHandler? = null,
	onDragEnd: DragEventHandler? = null,
	addAsChild: Boolean = true,
	className: String? = null,
	handler: StyledHandler<JMTreeItemProps>? = null
) = createStyled(treeItemComponent, addAsChild) {
	attrs.label = label
	attrs.nodeId = nodeId
	attrs.icon = icon
	attrs.onLabelClick = onLabelClick
	attrs.onDoubleClick = onDoubleClick
	attrs.draggable = onDragStart != null
	attrs.onDragStart = onDragStart
	attrs.onDragEnd = onDragEnd
	setStyledPropsAndRunHandler(className, handler)
}
