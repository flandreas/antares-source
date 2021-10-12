package ch.scorpion.jabbah.base.mreact

import com.ccfraser.muirwik.components.StyledPropsWithCommonAttributes
import com.ccfraser.muirwik.components.createStyled
import react.ComponentType
import react.RBuilder
import styled.StyledHandler

@JsModule("react-split-pane")
@JsNonModule
private external val splitPaneModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val splitPane: ComponentType<SplitPaneProps> = splitPaneModule.default

external interface SplitPaneProps : StyledPropsWithCommonAttributes {
	var split: String
	var defaultSize: Int?
	var minSize: Int?
	var maxSize: Int?
	var primary: String?
}

/**
 * Requires CSS from https://github.com/tomkp/react-split-pane to be existing.
 * Currently added to index.html of main application.
 */
fun RBuilder.splitPane(
	split: String,
	defaultSize: Int? = null,
	minSize: Int? = null,
	maxSize: Int? = null,
	primary: String? = null,
	className: String? = null,
	handler: StyledHandler<SplitPaneProps>? = null
) {
	createStyled(splitPane, className, handler) {
		attrs.split = split
		attrs.defaultSize = defaultSize
		attrs.minSize = minSize
		attrs.maxSize = maxSize
		primary?.let { attrs.primary = it }
	}
}
