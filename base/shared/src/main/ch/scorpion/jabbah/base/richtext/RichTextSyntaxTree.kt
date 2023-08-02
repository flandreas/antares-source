package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.AbstractNode
import ch.scorpion.jabbah.base.dsl.Compound
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.richtext.TextStyle.Companion.BOLD
import ch.scorpion.jabbah.base.richtext.TextStyle.Companion.NORMAL
import ch.scorpion.jabbah.base.richtext.TextStyle.Companion.OVERLINE
import ch.scorpion.jabbah.base.richtext.TextStyle.Companion.OVERLINE_BOLD

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

data class TextStyle(
	val overline: Boolean,
	val bold: Boolean
) {
	companion object {
		val NORMAL = TextStyle(overline = false, bold = false)
		val OVERLINE = TextStyle(overline = true, bold = false)
		val BOLD = TextStyle(overline = false, bold = true)
		val OVERLINE_BOLD = TextStyle(overline = true, bold = true)

		fun withOverline(style: TextStyle): TextStyle = of (true, style.bold)
		fun withoutOverline(style: TextStyle): TextStyle = of (false, style.bold)

		fun withBold(style: TextStyle): TextStyle = of(style.overline, true)
		fun withoutBold(style: TextStyle): TextStyle = of(style.overline, false)

		fun of(overline: Boolean, bold: Boolean): TextStyle =
			if (overline) {
				if (bold) OVERLINE_BOLD else OVERLINE
			} else {
				if (bold) BOLD else NORMAL
			}
	}
}

class StyledChunk(
	location: TextLocation,
	val text: String,
	val style: TextStyle = NORMAL
) : AbstractNode(location) {

	override fun toString(): String =
		when (style) {
			NORMAL -> text
			OVERLINE -> "${RichTextTokenType.OVERLINE.id}($text)"
			BOLD -> "${RichTextTokenType.BOLD.id}($text)"
			OVERLINE_BOLD -> "${RichTextTokenType.BOLD.id}${RichTextTokenType.OVERLINE.id}($text)"
			else -> throw IllegalArgumentException("unsupported style")
		}

	fun splitWords(): List<StyledChunk> {
		val words = text.split(' ')
		return words.mapIndexed { index: Int, s: String ->
			if (index == words.size - 1) {
				StyledChunk(location, s, style)
			} else {
				StyledChunk(location, "$s ", style)
			}
		}
	}
}