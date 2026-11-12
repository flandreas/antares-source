package io.antarescircuit.antares.view.addressable

/** A bounded window over Int-addressed memory, including the last address without integer overflow. */
internal data class MemoryPage(val start: Int, val maxAddress: Int, val cellsPerRow: Int) {
	companion object {
		const val MAX_ROWS = 1_024

		fun containing(address: Int, maxAddress: Int, cellsPerRow: Int): MemoryPage {
			require(address in 0..maxAddress)
			val size = MAX_ROWS * cellsPerRow
			return MemoryPage(address / size * size, maxAddress, cellsPerRow)
		}

		/**
		 * Parses [text] in the radix of [converter]. A `0x` prefix always selects hexadecimal; `0b` and `0d`
		 * select binary/decimal except in hexadecimal mode, where they are ordinary digits (e.g. "0B00").
		 */
		fun parseAddress(text: String, converter: AddressableValueConverter, maxAddress: Int): Int? {
			val value = text.trim()
			val defaultRadix = when (converter) {
				AddressableValueConverter.Hexadecimal -> 16
				AddressableValueConverter.Decimal -> 10
				AddressableValueConverter.Octal -> 8
				AddressableValueConverter.Binary -> 2
			}
			val prefixRadix = when {
				value.startsWith("0x", true) -> 16
				defaultRadix == 16 -> null
				value.startsWith("0b", true) -> 2
				value.startsWith("0d", true) -> 10
				else -> null
			}
			val digits = if (prefixRadix != null) value.drop(2) else value
			return digits.toLongOrNull(prefixRadix ?: defaultRadix)?.takeIf { it in 0..maxAddress.toLong() }?.toInt()
		}
	}

	val rowCount: Int get() = minOf(MAX_ROWS.toLong(), (maxAddress.toLong() - start + cellsPerRow) / cellsPerRow).toInt()
	val end: Int get() = minOf(maxAddress.toLong(), start.toLong() + MAX_ROWS * cellsPerRow - 1).toInt()
	val previous: Int? get() = if (start > 0) maxOf(0, start - MAX_ROWS * cellsPerRow) else null
	val next: Int? get() = if (end < maxAddress) end + 1 else null

	fun addressAt(row: Int, column: Int): Int? {
		if (row !in 0 until rowCount || column !in 0 until cellsPerRow) return null
		return (start.toLong() + row.toLong() * cellsPerRow + column).takeIf { it <= maxAddress }?.toInt()
	}
}
