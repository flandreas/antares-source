package ch.scorpion.antares.model.memory

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import java.util.regex.Pattern

/**
 * Represents a dump of a [Memory] in a single format consisting of a sequence of hexadecimal number, one for each
 * [Memory] address.
 *
 * Consists of a complete list of cells separated by blanks or newlines. The address of the cell values is
 * implicitly given by the cell's position. An optional cell comment is separated from the cell data value
 * using a colon.
 */
object MemoryDump {

	private val LOG by logger(MemoryDump::class)

	/** The character for delimiting individual cells.*/
	private const val CELL_DELIMITER = ' '

	/** The character for delimiting cell value and optional cell comment.*/
	private const val COMMENT_DELIMITER = ':'

	/** The regular expression for separating individual cells.*/
	private val cellSeparationRegex = Pattern.compile("(?<!\\\\)$CELL_DELIMITER|\n")

	/** The regular expression for separating cell value and optional cell comment.*/
	private val cellTokenSeparationRegex = Pattern.compile("(?<!\\\\)$COMMENT_DELIMITER")

	/**
	 * Writes the contents of the specified [Memory] into a new [String].
	 */
	fun write(memory: Memory, bitWidth: BitWidth): String {
		val builder = StringBuilder()
		val mask = BitOperation.power(bitWidth.width.toLong()) - 1L
		val length = Math.max(2, bitWidth.width / 4)

		val cellIter = ZeroFiller(memory.getNonZeroCells())
		while (cellIter.hasNext()) {
			val cell = cellIter.next()
			writePaddedHex(cell.value, mask, length, builder)
			if (StringUtils.isNotEmpty(cell.comment)) {
				builder.append(COMMENT_DELIMITER)
				writeEscapedComment(cell.comment!!, builder)
			}
			if (cellIter.hasNext()) {
				builder.append(CELL_DELIMITER)
			}
		}
		return builder.toString()
	}

	/**
	 * Reads the dump from the specified [String] into a [Memory].
	 */
	fun read(memory: Memory, dump: String) {
		memory.clear()
		for ((address, cell) in dump.split(cellSeparationRegex).withIndex()) {
			val cellTokens = cell.split(cellTokenSeparationRegex)
			when (cellTokens.size) {
				1 -> memory.write(address, BitOperation.hexToLong(cellTokens[0]))
				2 -> memory.writeCommentedValue(address, BitOperation.hexToLong(cellTokens[0]), readEscapedComment(cellTokens[1]))
				else -> {
					LOG.error("illegal syntax at address $address in cell $cell")
					throw IllegalArgumentException("Illegal syntax in MemoryDump")
				}
			}
		}
	}

	fun readNewlineSeparated(memory: Memory, dump: String) {
		memory.clear()
		var address = 0
		for (line in dump.split('\n')) {
			val commentDelimiterIndex = line.indexOf(COMMENT_DELIMITER)
			val comment: String?
			val value: Long
			if (commentDelimiterIndex == -1) {
				for (cell in line.split(CELL_DELIMITER)) {
					memory.write(address++, BitOperation.hexToLong(cell))
				}
			} else {
				value = BitOperation.hexToLong(line.substring(0, commentDelimiterIndex))
				comment = line.substring(commentDelimiterIndex + 1)
				memory.writeCommentedValue(address++, value, comment)
			}
		}
	}

	private fun writePaddedHex(value: Long, mask: Long, length: Int, builder: StringBuilder) {
		builder.append(BitOperation.longToHex(value and mask).toUpperCase().padStart(length, '0'))
	}

	private fun writeEscapedComment(comment: String, builder: StringBuilder) {
		builder.append(
			comment
				.replace(COMMENT_DELIMITER.toString(), "\\$COMMENT_DELIMITER")
				.replace(CELL_DELIMITER.toString(), "\\$CELL_DELIMITER"))
	}

	private fun readEscapedComment(comment: String): String {
		return comment
			.replace("\\$COMMENT_DELIMITER", COMMENT_DELIMITER.toString())
			.replace("\\$CELL_DELIMITER", CELL_DELIMITER.toString())
	}
}