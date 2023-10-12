package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.AbstractNode
import ch.scorpion.jabbah.base.dsl.Compound
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.richtext.TextStyle.Companion.NORMAL

class RichText(
	location: TextLocation,
	fragments: List<Fragment>
) : Compound<Fragment>(location, fragments) {

	companion object {

		/**
		 * Strips [text] encoded as rich text to plain text so that it can be used
		 * e.g. in file path strings. Strips most of the style information, especially
		 * the slashes.
		 */
		fun stripToPlainText(text: String): String {
			try {
				val richText = RichTextParser(text).parse()
				val result = StringBuilder()

				richText.children.forEach { fragment ->
					fragment.text.styledText.chunks.forEach { chunk ->
						val text = chunk.text.replace("/", " ")
						result.append(text)
					}
				}

				return result.toString()
			} catch (e: Throwable) {
				return text.replace("/", " ")
			}
			return text
		}
	}
}

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
	val bold: Boolean,
	val italic: Boolean
) {
	companion object {
		val NORMAL = TextStyle(overline = false, bold = false, false)
		val OVERLINE = TextStyle(overline = true, bold = false, false)
		val BOLD = TextStyle(overline = false, bold = true, false)
		val OVERLINE_BOLD = TextStyle(overline = true, bold = true, false)

		val ITALIC = TextStyle(overline = false, bold = false, true)
		val OVERLINE_ITALIC = TextStyle(overline = true, bold = false, true)
		val BOLD_ITALIC = TextStyle(overline = false, bold = true, true)
		val OVERLINE_BOLD_ITALIC = TextStyle(overline = true, bold = true, true)

		fun withOverline(style: TextStyle): TextStyle = of(true, style.bold, style.italic)
		fun withoutOverline(style: TextStyle): TextStyle = of(false, style.bold, style.italic)

		fun withBold(style: TextStyle): TextStyle = of(style.overline, true, style.italic)
		fun withoutBold(style: TextStyle): TextStyle = of(style.overline, false, style.italic)

		fun withItalic(style: TextStyle): TextStyle = of(style.overline, style.bold, true)
		fun withoutItalic(style: TextStyle): TextStyle = of(style.overline, style.bold, false)

		fun of(overline: Boolean, bold: Boolean, italic: Boolean): TextStyle =
			if (overline) {
				if (bold) {
					if (italic) OVERLINE_BOLD_ITALIC else OVERLINE_BOLD
				} else {
					if (italic) OVERLINE_ITALIC else OVERLINE
				}
			} else {
				if (bold) {
					if (italic) BOLD_ITALIC else BOLD
				} else {
					if (italic) ITALIC else NORMAL
				}
			}
	}
}

class StyledChunk(
	location: TextLocation,
	val text: String,
	val style: TextStyle = NORMAL
) : AbstractNode(location) {

	override fun toString(): String {
		val s = StringBuilder()
		if (style.bold) {
			s.append(RichTextTokenType.BOLD.id)
		}
		if (style.italic) {
			s.append(RichTextTokenType.ITALIC.id)
		}
		if (style.overline) {
			s.append(RichTextTokenType.OVERLINE.id)
		}
		if (s.isNotEmpty()) {
			s.append("($text)")
		} else {
			s.append(text)
		}
		return s.toString()
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