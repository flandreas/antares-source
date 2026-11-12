package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.model.signal.Bit

/**
 * Plain-text interchange for spreadsheet selections, accepting TSV, CSV, or space-separated bits.
 * Accepts "Z" (undefined) as well, since copied undefined cells are exported that way.
 */
object TruthTableClipboard {

	fun parse(text: String): List<List<Bit>> {
		val lines = text.trim('\r', '\n').lines()
		require(lines.isNotEmpty() && lines.none { it.isBlank() }) { "Empty truth table data" }
		val values = lines.map { line ->
			val cells = when {
				'\t' in line -> line.split('\t')
				',' in line -> line.split(',')
				else -> line.trim().split(Regex("\\s+"))
			}
			cells.map { cell ->
				when (cell.trim().uppercase()) {
					"0" -> Bit.False
					"1" -> Bit.True
					"X" -> Bit.Error
					"Z" -> Bit.Undefined
					else -> throw IllegalArgumentException("Expected 0, 1, X, or Z")
				}
			}
		}
		require(values.all { it.size == values.first().size }) { "Unequal truth table row widths" }
		return values
	}
}
