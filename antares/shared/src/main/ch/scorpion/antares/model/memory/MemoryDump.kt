package ch.scorpion.antares.model.memory

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import kotlin.math.max

/**
 * Represents a dump of a [Memory] in a single format consisting of a sequence of hexadecimal number, one for each
 * [Memory] address.
 *
 * Consists of a complete list of cells separated by blanks or newlines. The address of the cell values is
 * implicitly given by the cell's position. An optional cell comment is separated from the cell data value
 * using a colon.
 *
 * The current code doesn't write a [MemoryDumpFileVersion] into the dump. This wont be necessary
 * before the first format change is invented. Dumps without a version info will forever be
 * regarded as [MemoryDumpVersionType.Default] of version number 0.1.
 */
object MemoryDump {

	private val LOG by logger(MemoryDump::class)

	/** The file format version supported by the current code.*/
	@Suppress("unused")
	private const val CURRENT_VERSION = 0.1

	/** The character for delimiting individual cells.*/
	private const val CELL_DELIMITER = ' '

	/** The character for delimiting cell value and optional cell comment.*/
	private const val CELL_COMMENT_DELIMITER = ':'

	/**
	 * The character for starting a line comment. Must be the first character of the line.
	 * All subsequent characters of the same line are ignored.
	 */
	const val LINE_COMMENT_CHAR = '#'

	/** The regular expression for separating individual cells.*/
	private val cellSeparationRegex = Regex("(?<!\\\\)$CELL_DELIMITER|\n")

	/** The regular expression for separating cell value and optional cell comment.*/
	private val cellTokenSeparationRegex = Regex("(?<!\\\\)$CELL_COMMENT_DELIMITER")

	/**
	 * Writes the contents of the specified [Memory] into a new [String] in [MemoryDumpVersionType.Default] format.
	 */
	fun write(memory: Memory, bitWidth: BitWidth): String {
		val builder = StringBuilder()
		val mask = BitOperation.power(bitWidth.width.toByte()) - 1L
		val length = max(2, bitWidth.width / 4)

		val cellIter = ZeroFiller(memory.getNonZeroCells())
		while (cellIter.hasNext()) {
			val cell = cellIter.next()
			writePaddedHex(cell.value, mask, length, builder)
			if (StringUtils.isNotEmpty(cell.comment)) {
				builder.append(CELL_COMMENT_DELIMITER)
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
		val version = MemoryDumpFileVersion.extractFrom(dump)

		return if (version?.type == MemoryDumpVersionType.Newline) {
			readNewLineFormat(memory, dump)
		} else {
			readStandardFormat(memory, dump)
		}
	}

	private fun removeCommentLines(dump: String): String {
		val result = StringBuilder()
		for (line in dump.split('\n')) {
			if (!line.startsWith(LINE_COMMENT_CHAR)) {
				result.append(line).append('\n')
			}
		}
		return result.toString()
	}

	private fun readStandardFormat(memory: Memory, dump: String) {
		memory.clear()
		for ((address, cell) in removeCommentLines(dump).split(cellSeparationRegex).withIndex()) {
			val cellTokens = cell.split(cellTokenSeparationRegex)
			when (cellTokens.size) {
				1 -> memory.write(address, BitOperation.hexToLong(cellTokens[0]))
				2 -> memory.writeCommentedValue(address, BitOperation.hexToLong(cellTokens[0]), readEscapedCellComment(cellTokens[1]))
				else -> {
					LOG.error("illegal syntax at address $address in cell $cell")
					throw IllegalArgumentException("Illegal syntax in MemoryDump")
				}
			}
		}
	}

	private fun readNewLineFormat(memory: Memory, dump: String) {
		memory.clear()
		var address = 0
		for (line in removeCommentLines(dump).split('\n')) {
			val commentDelimiterIndex = line.indexOf(CELL_COMMENT_DELIMITER)
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
				.replace(CELL_COMMENT_DELIMITER.toString(), "\\$CELL_COMMENT_DELIMITER")
				.replace(CELL_DELIMITER.toString(), "\\$CELL_DELIMITER"))
	}

	private fun readEscapedCellComment(comment: String): String {
		return comment
			.replace("\\$CELL_COMMENT_DELIMITER", CELL_COMMENT_DELIMITER.toString())
			.replace("\\$CELL_DELIMITER", CELL_DELIMITER.toString())
	}
}

enum class MemoryDumpVersionType(val identifier: String) {
	Default("amd-df"),
	Newline("amd-nl");

	companion object {
		fun of(dump: String): MemoryDumpVersionType {
			return if (Newline.canRecognize(dump)) {
				Newline
			} else {
				Default
			}
		}
	}

	fun canRecognize(dump: String): Boolean {
		return dump.startsWith(identifier, ignoreCase = true)
	}
}

data class MemoryDumpFileVersion(
	val type: MemoryDumpVersionType,
	val number: String
) {
	companion object {

		private const val LENGTH = 11
		private val regex = """${MemoryDump.LINE_COMMENT_CHAR}(amd-df|amd-nl)-(.*)""".toRegex()

		fun extractFrom(dump: String): MemoryDumpFileVersion? {
			if (dump.length < LENGTH) {
				return null
			}

			val version = dump.substring(0 until LENGTH)
			val result = regex.matchEntire(version)

			if (result == null || result.groupValues.size != 3) {
				return null
			}

			return MemoryDumpFileVersion(
				MemoryDumpVersionType.of(result.groupValues[1]),
				result.groupValues[2])
		}
	}
}