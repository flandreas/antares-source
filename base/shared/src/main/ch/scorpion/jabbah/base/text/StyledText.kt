package ch.scorpion.jabbah.base.text

/**
 * A sequence of text [Chunk]s each with different styles.
 * Currently supports only 'bold' as non-standard style.
 *
 * Use [StyledTextBuilder] for convenient building instances of [StyledText]
 */
interface StyledText {

	/** The entire number of characters across all [Chunk]s. */
	val length: Int

	/** `true` if this [StyledText] doesn't contain any characters.*/
	val empty: Boolean get() = length == 0

	/** `true` if this [StyledText] contains at least one characters. */
	val notEmpty: Boolean get() = !empty

	val chunkCount: Int

	fun getChunks(): Iterator<Chunk>

	/** Splits this [StyledText] at end-of-lines and returns a new [StyledText] for each individual line.*/
	fun splitLines(): List<StyledText>

	/** Splits this [StyledText] at the specified delimiter characters.*/
	fun split(vararg delimiters: Char): List<StyledText>

	/**
	 * Returns `true` if the character at [index] is to be rendered bold. Currently only used
	 * for naive and simple [Chunk] rendering without inter-word style changes.
	 */
	fun isBold(index: Int): Boolean

	/** Returns the combined text of all [Chunk]s as a single text. Currently only used for
	 * faive and simple [Chunk] rendering without inter-word style changes.*/
	fun asPlainText(): String
}

interface Chunk {
	val text: String
	val bold: Boolean
}

private class StyledTextImpl(
	private val chunks: MutableList<Chunk> = mutableListOf()
) : StyledText {

	override val length: Int get() = chunks.sumOf { it.text.length }

	override val chunkCount: Int get() = chunks.size

	override fun getChunks(): Iterator<Chunk> = chunks.iterator()

	override fun isBold(index: Int): Boolean = getChunkAt(index).bold

	override fun split(vararg delimiters: Char): List<StyledText> {
		val result = mutableListOf<StyledText>()
		for (chunk in chunks) {
			chunk.text.split(*delimiters).forEach { text ->
				if (text.isNotBlank()) {
					val newStyledText = StyledTextImpl()
					newStyledText.addText(text, chunk.bold)
					result.add(newStyledText)
				}
			}
		}
		return result
	}

	override fun splitLines(): List<StyledText> {
		val chunkLines = mutableListOf<MutableList<Chunk>>()
		for (chunk in chunks) {
			if (chunk.text.contains('\n')) {
				chunk.text.split('\n').forEach { text ->
					chunkLines.add(mutableListOf(ChunkImpl(text, chunk.bold)))
				}
			} else {
				if (chunkLines.isEmpty()) {
					chunkLines.add(mutableListOf())
				}
				chunkLines.last().add(chunk)
			}
		}
		return chunkLines.map { StyledTextImpl(it) }
	}

	override fun asPlainText(): String {
		val b = StringBuilder()
		chunks.forEach { b.append(it.text) }
		return b.toString()
	}

	fun addText(text: String, bold: Boolean = false): StyledText {
		if (text.isEmpty()) {
			return this
		}
		if (chunks.isEmpty() || chunks.last().bold != bold) {
			chunks.add(ChunkImpl(text, bold))
		} else {
			val last = chunks.removeLast()
			chunks.add(ChunkImpl("${last.text}${text}", bold))
		}
		return this
	}

	fun add(other: StyledText): StyledText {
		for (chunk in other.getChunks()) {
			addText(chunk.text, chunk.bold)
		}
		return this
	}

	private fun getChunkAt(index: Int): Chunk {
		var i = 0
		var chunkIndex = 0
		while (index > i + chunks[chunkIndex].text.length - 1) {
			i += chunks[chunkIndex].text.length
			chunkIndex++
		}
		return chunks[chunkIndex]
	}

	private data class ChunkImpl(
		override val text: String,
		override val bold: Boolean = false
	) : Chunk
}

class StyledTextBuilder {

	private val styledText = StyledTextImpl()
	private var bold = false

	val empty: Boolean get() = styledText.empty
	val notEmpty: Boolean get() = styledText.notEmpty

	/** Sets the 'bold' style. Subsequently added text will be bold.*/
	fun beginBold(): StyledTextBuilder {
		bold = true
		return this
	}

	/** Resets the 'bold' style. Subsequently added text won't be bold any more.*/
	fun endBold(): StyledTextBuilder {
		bold = false
		return this
	}

	fun append(text: String): StyledTextBuilder {
		styledText.addText(text, bold)
		return this
	}

	fun append(c: Char): StyledTextBuilder {
		styledText.addText(c.toString(), bold)
		return this
	}

	fun appendLine(): StyledTextBuilder {
		styledText.addText("\n")
		return this
	}

	/** Adds [text] as bold. Subsequently added text won't be bold any more.*/
	fun appendBold(text: String): StyledTextBuilder {
		styledText.addText(text, bold = true)
		return this
	}

	fun append(other: StyledText): StyledTextBuilder {
		styledText.add(other)
		return this
	}

	fun build(): StyledText = styledText
}