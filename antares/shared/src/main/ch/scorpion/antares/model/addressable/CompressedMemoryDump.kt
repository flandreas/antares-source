package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.StringUtils
import kotlin.math.max


/**
 * Represents a dump of a [Memory] in an Antares proprietary, compressed textual format.
 */
object CompressedMemoryDump {

	private const val CARDINALITY_DELIMITER = '*'

	/** The character for delimiting individual cells.*/
	private const val CELL_DELIMITER = ' '

	/** The character for delimiting cell value and optional cell comment.*/
	private const val COMMENT_DELIMITER = ':'

	/** The regular expression for separating individual cells.*/
	private val cellSeparationRegex by lazy { Regex("(?<!\\\\)$CELL_DELIMITER|\n") }

	/**
	 * Writes a dump of a [Memory] to a new [String].
	 */
	fun write(memory: Memory, bitWidth: BitWidth): String {
		val builder = StringBuilder()
		val mask: ULong = bitWidth.maxValue
		val length = max(2, bitWidth.width / 4)

		val cellIter = ZeroFiller(memory.getNonZeroCells())
		if (!cellIter.hasNext()) {
			return ""
		}

		var lastWrittenCell: MemoryCell? = null
		var lastCell: MemoryCell? = null
		while (cellIter.hasNext()) {
			val cell = cellIter.next()
			var lastAddress = lastWrittenCell?.address ?: -1
			if (lastCell != null && (cell.value != lastCell.value || cell.comment != lastCell.comment)) {
				if (lastWrittenCell != null) {
					builder.append(CELL_DELIMITER)
				}
				write(lastCell.value, lastCell.comment, cell.address - lastAddress - 1, mask, length, builder)
				lastWrittenCell = lastCell
			}

			lastAddress = lastWrittenCell?.address ?: -1
			if (cellIter.hasNext()) {
				lastCell = cell
			} else {
				if (lastWrittenCell != null) {
					builder.append(CELL_DELIMITER)
				}

				write(cell.value, cell.comment, cell.address - lastAddress, mask, length, builder)
			}
		}

		return builder.toString()
	}

	private fun write(value: ULong, comment: String?, count: Int, mask: ULong, length: Int, builder: StringBuilder) {
		if (count > 1) {
			builder.append(count.toString())
			builder.append(CARDINALITY_DELIMITER)
		}
		writePaddedHex(value, mask, length, builder)
		if (StringUtils.isNotEmpty(comment)) {
			builder.append(COMMENT_DELIMITER)
			writeEscapedComment(comment!!, builder)
		}
	}

	private fun writePaddedHex(value: ULong, mask: ULong, length: Int, builder: StringBuilder) {
		builder.append(BitOperation.longToHex(value and mask).uppercase().padStart(length, '0'))
	}

	private fun writeEscapedComment(comment: String, builder: StringBuilder) {
		builder.append(
			comment
				.replace(COMMENT_DELIMITER.toString(), "\\$COMMENT_DELIMITER")
				.replace(CELL_DELIMITER.toString(), "\\$CELL_DELIMITER"))
	}


	/**
	 * Reads a dump into a [Memory] from an external dump.
	 */
	fun read(memory: Memory, dump: String) {
		memory.clear()
		if (dump.isNotBlank()) {
			var address = 0
			for (cell in dump.split(cellSeparationRegex)) {
				address = read(cell, address, memory)
			}
		}
	}

	private fun read(cell: String, address: Int, memory: Memory): Int {
		var lAddress = address

		val cardinalityDelimiterIndex = cell.indexOf(CARDINALITY_DELIMITER)
		val cellContent: String = if (cardinalityDelimiterIndex == -1) {
			cell
		} else {
			cell.substring(cardinalityDelimiterIndex + 1)
		}

		val commentDelimiterIndex = cellContent.indexOf(COMMENT_DELIMITER)
		val comment: String?
		val value: ULong
		if (commentDelimiterIndex == -1) {
			value = BitOperation.hexToLong(cellContent)
			comment = null
		} else {
			value = BitOperation.hexToLong(cellContent.substring(0, commentDelimiterIndex))
			comment = readEscapedComment(cellContent.substring(commentDelimiterIndex + 1))
		}

		if (cardinalityDelimiterIndex == -1) {
			memory.writeCommentedValue(lAddress++, value, comment)
		} else {
			val count = cell.substring(0, cardinalityDelimiterIndex).toInt()
			for (i in 0 until count) {
				memory.writeCommentedValue(lAddress++, value, comment)
			}
		}
		return lAddress
	}

	private fun readEscapedComment(comment: String): String {
		return comment
			.replace("\\$COMMENT_DELIMITER", COMMENT_DELIMITER.toString())
			.replace("\\$CELL_DELIMITER", CELL_DELIMITER.toString())
	}
}