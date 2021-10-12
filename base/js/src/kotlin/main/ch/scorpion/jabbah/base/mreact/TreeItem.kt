package ch.scorpion.jabbah.base.mreact

import ch.scorpion.jabbah.base.event.DragEventHandler
import ch.scorpion.jabbah.base.event.MouseEventHandler
import com.ccfraser.muirwik.components.StyledPropsWithCommonAttributes
import com.ccfraser.muirwik.components.createStyled
import org.w3c.dom.events.MouseEvent
import react.*
import styled.StyledHandler

@JsModule("@material-ui/lab/TreeItem")
@JsNonModule
private external val treeItemModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val treeItemComponent: ComponentType<JMTreeItemProps> = treeItemModule.default

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
	className: String? = null,
	handler: StyledHandler<JMTreeItemProps>? = null
) = createStyled(treeItemComponent, className, handler) {
	attrs.label = label
	attrs.nodeId = nodeId
	attrs.icon = icon
	attrs.onLabelClick = onLabelClick
	attrs.onDoubleClick = onDoubleClick
	attrs.draggable = onDragStart != null
	attrs.onDragStart = onDragStart
	attrs.onDragEnd = onDragEnd
}
