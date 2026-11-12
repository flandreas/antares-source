package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.model.signal.Bit

/** Plain-text interchange for spreadsheet selections, accepting TSV, CSV, or space-separated bits. */
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
					else -> throw IllegalArgumentException("Expected 0, 1, or X")
				}
			}
		}
		require(values.all { it.size == values.first().size }) { "Unequal truth table row widths" }
		return values
	}
}
