package io.antarescircuit.jabbah.base.richtext

import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.base.dsl.AbstractNode
import io.antarescircuit.jabbah.base.dsl.Compound
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.base.richtext.TextStyle.Companion.NORMAL
import io.antarescircuit.jabbah.base.parser.TextLocation.Companion.UNDEFINED
import kotlin.math.max

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
			return try {
				val richText = RichTextParser(text).parse()
				val result = StringBuilder()

				richText.children.forEach { fragment ->
					fragment.text.styledText.chunks.forEach { chunk ->
						result.append(chunk.text.replace("/", " "))
					}
				}

				result.toString()
			} catch (_: Throwable) {
				text.replace("/", " ")
			}
		}

		/**
		 * Treats the text as plain text and creates a [RichText] object that ignores all markup.
		 */
		fun asPlain(text: String) =
			RichText(
				UNDEFINED,
				listOf(
					Fragment(
						UNDEFINED,
						FragmentText(
							UNDEFINED,
							StyledText(
								UNDEFINED,
								listOf(StyledChunk(UNDEFINED, text))
							)
						)
					)
				)
			)
	}

	fun getMaxOverlineLevel() =
		children.maxOfOrNull {
			it.text.styledText.chunks.maxOfOrNull { chunk -> chunk.style.overlineLevel } ?: 0
		} ?: 0
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

@Suppress("MemberVisibilityCanBePrivate")
data class TextStyle(
	val overlineLevel: Int,
	val bold: Boolean,
	val italic: Boolean
) {
	companion object {
		val NORMAL = TextStyle(overlineLevel = 0, bold = false, italic = false)

		fun pushOverline(style: TextStyle) = TextStyle(style.overlineLevel + 1, style.bold, style.italic)
		fun popOverline(style: TextStyle) = TextStyle(max(style.overlineLevel - 1, 0), style.bold, style.italic)

		fun withBold(style: TextStyle) = TextStyle(style.overlineLevel, true, style.italic)
		fun withoutBold(style: TextStyle) = TextStyle(style.overlineLevel, false, style.italic)

		fun withItalic(style: TextStyle) = TextStyle(style.overlineLevel, style.bold, true)
		fun withoutItalic(style: TextStyle) = TextStyle(style.overlineLevel, style.bold, false)
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
        (1 .. style.overlineLevel).forEach { _ -> s.append(RichTextTokenType.OVERLINE.id) }
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