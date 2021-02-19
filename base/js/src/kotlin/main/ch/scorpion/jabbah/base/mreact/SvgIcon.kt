package ch.scorpion.jabbah.base.mreact

import kotlinx.html.SVG
import react.*
import react.dom.RDOMBuilder
import react.dom.svg

open class SvgPathProps(val d: String, val stroke: String = "black"): RProps

fun RBuilder.svgIcon(
	width: Int,
	height: Int,
	viewBox: String,
	fill: String = "none",
	handler: (RDOMBuilder<SVG>) -> Unit
): ReactElement {
	return svg {
		attrs["width"] = width.toString()
		attrs["height"] = height.toString()
		attrs["viewBox"] = viewBox
		attrs["fill"] = fill
		attrs["xmlns"] = "http://www.w3.org/2000/svg"

		apply(handler)
	}
}

fun RDOMBuilder<SVG>.svgPath(
	d: String,
	stroke: String = "black"
) {
	child(createElement("path", SvgPathProps(d, stroke)))
}
