package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.model.signal.BitOperation
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.StringUtils
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

		val cells = memory.getNonZeroCells()
		var nextAddress = 0L
		var runValue = 0UL
		var runComment: String? = null
		var runCount = 0L
		fun flush() {
			if (runCount == 0L) return
			if (builder.isNotEmpty()) builder.append(CELL_DELIMITER)
			write(runValue, runComment, runCount, mask, length, builder)
			runCount = 0
		}
		fun appendRun(value: ULong, comment: String?, count: Long) {
			if (count == 0L) return
			if (runCount > 0 && (runValue != value || runComment != comment)) flush()
			runValue = value
			runComment = comment
			runCount += count
		}
		while (cells.hasNext()) {
			val cell = cells.next()
			appendRun(0UL, null, cell.address - nextAddress)
			appendRun(cell.value, cell.comment, 1)
			nextAddress = cell.address.toLong() + 1
		}
		flush()

		return builder.toString()
	}

	private fun write(value: ULong, comment: String?, count: Long, mask: ULong, length: Int, builder: StringBuilder) {
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
			var address = 0L
			for (cell in dump.split(cellSeparationRegex)) {
				address = read(cell, address, memory)
			}
		}
	}

	private fun read(cell: String, address: Long, memory: Memory): Long {

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

		val count = if (cardinalityDelimiterIndex == -1) 1L else cell.substring(0, cardinalityDelimiterIndex).toLong()
		require(count > 0 && count <= Int.MAX_VALUE.toLong() + 1 - address) { "Memory image exceeds address capacity" }
		// Unallocated zeros are implicit; never expand a sparse gap while loading a circuit.
		if (value != 0UL || comment != null) {
			var offset = 0L
			while (offset < count) memory.writeCommentedValue((address + offset++).toInt(), value, comment)
		}
		return address + count
	}

	private fun readEscapedComment(comment: String): String {
		return comment
			.replace("\\$COMMENT_DELIMITER", COMMENT_DELIMITER.toString())
			.replace("\\$CELL_DELIMITER", CELL_DELIMITER.toString())
	}
}