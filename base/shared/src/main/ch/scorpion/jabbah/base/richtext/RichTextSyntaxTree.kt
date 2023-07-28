package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.AbstractNode
import ch.scorpion.jabbah.base.dsl.Compound
import ch.scorpion.jabbah.base.parser.TextLocation

class RichText(
	location: TextLocation,
	fragments: List<Fragment>
) : Compound<Fragment>(location, fragments)

class Fragment(
	location: TextLocation,
	val text: FragmentText,
	val subscript: Subscript? = null,
	val superscript: Superscript? = null
) : AbstractNode(location) {

	override fun toString(): String = "Fragment"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			text.accept(visitor)
			subscript?.accept(visitor)
			superscript?.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}

abstract class AbstractFragmentPart(
	location: TextLocation,
	val styledText: StyledText
) : AbstractNode(location) {

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			styledText.chunks.forEach {
				it.accept(visitor)
			}
		}
		return visitor.visitLeave(this)
	}
}

class FragmentText(
	location: TextLocation,
	styledText: StyledText
) : AbstractFragmentPart(location, styledText) {

	override fun toString(): String = "-"
}

class Subscript(
	location: TextLocation,
	styledText: StyledText
) : AbstractFragmentPart(location, styledText) {

	override fun toString(): String = "_"
}

class Superscript(
	location: TextLocation,
	styledText: StyledText
) : AbstractFragmentPart(location, styledText) {

	override fun toString(): String = "^"
}

class StyledText(
	location: TextLocation,
	val chunks: List<StyledChunk>
) : AbstractNode(location) {

	override fun toString(): String = "StyledText"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			chunks.forEach { it.accept(visitor) }
		}
		return visitor.visitLeave(this)
	}
}

enum class TextStyle {
	NORMAL,
	OVERLINE,
	BOLD
}

class StyledChunk(
	location: TextLocation,
	val text: String,
	val style: TextStyle = TextStyle.NORMAL
) : AbstractNode(location) {

	override fun toString(): String {
		return when (style) {
			TextStyle.NORMAL -> text
			TextStyle.OVERLINE -> "${RichTextTokenType.OVERLINE.id}($text)"
			TextStyle.BOLD -> "${RichTextTokenType.BOLD.id}($text)"
		}
	}
}