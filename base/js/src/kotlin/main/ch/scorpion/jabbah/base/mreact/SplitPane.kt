package ch.scorpion.jabbah.base.mreact

import com.ccfraser.muirwik.components.StyledPropsWithCommonAttributes
import com.ccfraser.muirwik.components.createStyled
import com.ccfraser.muirwik.components.setStyledPropsAndRunHandler
import react.*
import styled.StyledHandler

@JsModule("react-split-pane")
@JsNonModule
private external val splitPaneModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val splitPane: RComponent<SplitPaneProps, RState> = splitPaneModule.default

interface SplitPaneProps : StyledPropsWithCommonAttributes {
	var split: String
	var defaultSize: Int?
	var minSize: Int?
}

/**
 * Requires CSS from https://github.com/tomkp/react-split-pane to be existing.
 * Currently added to index.html of main application.
 */
fun RBuilder.splitPane(
	split: String,
	defaultSize: Int? = null,
	minSize: Int? = null,
	addAsChild: Boolean = true,
	className: String? = null,
	handler: StyledHandler<SplitPaneProps>? = null
) = createStyled(splitPane, addAsChild) {
	attrs.split = split
	attrs.defaultSize = defaultSize
	attrs.minSize = minSize
	setStyledPropsAndRunHandler(className, handler)
}
