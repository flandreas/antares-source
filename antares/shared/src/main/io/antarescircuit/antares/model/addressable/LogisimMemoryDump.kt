package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.model.signal.BitWidth

/** Interchange formats for Logisim memory images. Antares cell comments are not exported. */
object LogisimMemoryDump {

	private const val RAW = "v2.0 raw"
	private const val PLAIN = "v3.0 hex words plain"
	private const val ADDRESSED = "v3.0 hex words addressed"
	private val whitespace = Regex("\\s+")

	fun recognizes(dump: String): Boolean = dump.trimStart('\uFEFF', ' ', '\t', '\r', '\n').startsWith("v", ignoreCase = true)

	/** Parses into temporary memory; malformed files never alter the destination. */
	fun read(memory: Memory, dump: String, maxAddress: Int = Int.MAX_VALUE, maxValue: ULong = ULong.MAX_VALUE) {
		require(maxAddress >= 0) { "Memory capacity must be nonnegative" }
		val lines = dump.trimStart('\uFEFF', ' ', '\t', '\r', '\n').lineSequence().iterator()
		require(lines.hasNext()) { "Missing memory image header" }
		val header = lines.next().substringBefore('#').trim().lowercase().split(whitespace).joinToString(" ")
		require(header == RAW || header == PLAIN || header == ADDRESSED) { "Unsupported memory image format: $header" }
		val parsed = Memory()
		var address = 0L
		var lineNumber = 1
		for (sourceLine in lines) {
			lineNumber++
			var line = sourceLine.substringBefore('#').trim()
			if (header != RAW) line = line.substringBefore("  ")
			if (line.isEmpty()) continue
			if (header == ADDRESSED) {
				val separator = line.indexOf(':')
				require(separator > 0) { "Line $lineNumber: expected hexadecimal address followed by ':'" }
				address = hex(line.substring(0, separator).trim(), lineNumber).let {
					require(it <= maxAddress.toULong()) { "Line $lineNumber: address exceeds memory capacity" }
					it.toLong()
				}
				line = line.substring(separator + 1).trim()
			}
			if (line.isEmpty()) continue
			for (token in line.split(whitespace)) {
				val parts = token.split('*')
				require(parts.size == 1 || header == RAW && parts.size == 2) { "Line $lineNumber: invalid word '$token'" }
				val count = if (parts.size == 2) parts[0].toLongOrNull() else 1L
				require(count != null && count > 0 && count <= maxAddress.toLong() - address + 1) {
					"Line $lineNumber: word count exceeds memory capacity"
				}
				val value = hex(parts.last(), lineNumber)
				require(value <= maxValue) { "Line $lineNumber: word '$token' exceeds the data bit width" }
				// Zero runs need no allocation, even for large sparse images.
				if (value != 0UL || header == ADDRESSED) {
					var offset = 0L
					while (offset < count) parsed.write((address + offset++).toInt(), value)
				}
				address += count
			}
		}
		memory.replaceWith(parsed)
	}

	/** Writes sparse addressed words, proportional to stored cells rather than address-space size. */
	fun writeAddressed(memory: Memory, bitWidth: BitWidth): String = buildString {
		append(ADDRESSED).append('\n')
		val digits = (bitWidth.width + 3) / 4
		val cells = memory.getNonZeroCells()
		while (cells.hasNext()) {
			val cell = cells.next()
			if (cell.value == 0UL) continue
			append(cell.address.toString(16)).append(": ")
			append((cell.value and bitWidth.maxValue).toString(16).padStart(digits, '0')).append('\n')
		}
	}

	/** Writes the widely supported raw format with run-length encoded zero gaps and repeated values. */
	fun writeRaw(memory: Memory, bitWidth: BitWidth): String = buildString {
		append(RAW).append('\n')
		var nextAddress = 0L
		var runValue = 0UL
		var runCount = 0L
		fun flush() {
			if (runCount == 0L) return
			if (runCount > 1) append(runCount).append('*')
			append(runValue.toString(16).padStart((bitWidth.width + 3) / 4, '0')).append('\n')
			runCount = 0
		}
		val cells = memory.getNonZeroCells()
		while (cells.hasNext()) {
			val cell = cells.next()
			val value = cell.value and bitWidth.maxValue
			if (value == 0UL) continue
			if (cell.address.toLong() != nextAddress) {
				flush()
				runValue = 0UL
				runCount = cell.address - nextAddress
				flush()
			}
			if (runCount != 0L && runValue != value) flush()
			runValue = value
			runCount++
			nextAddress = cell.address.toLong() + 1
		}
		flush()
	}

	private fun hex(token: String, line: Int): ULong =
		token.removePrefix("0x").removePrefix("0X").toULongOrNull(16)
			?: throw IllegalArgumentException("Line $line: invalid hexadecimal value '$token'")
}
