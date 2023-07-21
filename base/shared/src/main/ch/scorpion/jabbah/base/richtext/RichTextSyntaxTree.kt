package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.dsl.AbstractNode
import ch.scorpion.jabbah.base.parser.TextLocation

class Fragment(
	location: TextLocation,
	val text: StyledText,
	val subscript: StyledText? = null,
	val superscript: StyledText? = null
) : AbstractNode(location) {

}

class StyledText(location: TextLocation, val text: String) : AbstractNode(location) {

}