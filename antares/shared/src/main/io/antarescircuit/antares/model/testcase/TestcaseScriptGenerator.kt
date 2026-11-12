package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.testcase.parser.TestcaseLexer
import io.antarescircuit.antares.model.truthtable.TruthTable

/**
 * Creates [Testcase] scripts from [TruthTable]s.
 */
object TestcaseScriptGenerator {

	private val PLAIN_NAME = "[A-Za-z][A-Za-z0-9_]*".toRegex()

	/**
	 * Returns a test script for the combinational [truthTable]: A header line with the input and then the output
	 * column names, followed by one test vector per row. Don't care outputs are written as "X".
	 */
	fun generate(truthTable: TruthTable): String {
		require(truthTable.stateColumnCount == 0) { "Sequential truth tables are not supported" }

		val columns = 0 until truthTable.columnCount
		val names = columns.map { quoteIfNeeded(truthTable.getColumnName(it)) }
		val widths = names.map { it.length }
		val lines = listOf(names) + (0 until truthTable.rowsCount).map { row ->
			columns.map { valueOf(truthTable.getValue(row, it)) }
		}
		return lines.joinToString("\n", postfix = "\n") { cells ->
			cells.mapIndexed { column, cell -> cell.padEnd(widths[column]) }.joinToString(" ").trimEnd()
		}
	}

	/** Quotes names that would otherwise not be scanned as a single identifier, such as "!Q" or "run".*/
	private fun quoteIfNeeded(name: String): String =
		if (PLAIN_NAME.matches(name) && name !in TestcaseLexer.RESERVED_KEYWORDS) name else "'$name'"

	private fun valueOf(bit: Bit): String =
		when (bit) {
			Bit.False -> "0"
			Bit.True -> "1"
			Bit.Error -> "X"
			Bit.Undefined -> "Z"
		}
}
