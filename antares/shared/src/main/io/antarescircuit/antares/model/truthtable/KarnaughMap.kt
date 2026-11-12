package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.model.quinemccluskey.DNF
import io.antarescircuit.antares.model.quinemccluskey.Literal
import kotlin.math.abs
import kotlin.math.max

/** A cell of a [KarnaughMap]. [submap] selects one of the side-by-side grids used for more than four variables. */
data class KarnaughCell(val submap: Int, val row: Int, val column: Int)

/** A rectangular block of cells within one submap of a [KarnaughMap]. */
data class KarnaughRectangle(
	val submap: Int,
	val row: Int,
	val column: Int,
	val rowSpan: Int,
	val columnSpan: Int,
)

/**
 * The layout of a Karnaugh map for [variableCount] [TruthTable] inputs, where variable `i` is input column `i`.
 *
 * Variables beyond four select one of [submapCount] side-by-side 4x4 submaps (the leading variables).
 * Of the remaining variables, the first half labels the rows and the rest labels the columns.
 * Submaps, rows and columns are ordered in Gray code, so neighboring cells, including those wrapping around
 * the edges, differ in exactly one variable. Codes are read with the first variable as the most significant bit.
 */
class KarnaughMap(val variableCount: Int) {

	companion object {
		const val MAX_VARIABLES = 6

		fun grayCode(index: Int): Int = index xor (index shr 1)

		fun grayIndex(code: Int): Int {
			var index = 0
			var c = code
			while (c != 0) {
				index = index xor c
				c = c shr 1
			}
			return index
		}
	}

	init {
		require(variableCount in 1..MAX_VARIABLES) { "Karnaugh maps support 1 to $MAX_VARIABLES variables" }
	}

	val submapVariableCount: Int = max(0, variableCount - 4)

	val rowVariableCount: Int = (variableCount - submapVariableCount) / 2

	val columnVariableCount: Int = variableCount - submapVariableCount - rowVariableCount

	val submapCount: Int = 1 shl submapVariableCount

	val rowCount: Int = 1 shl rowVariableCount

	val columnCount: Int = 1 shl columnVariableCount

	val submapVariables: List<Int> = (0 until submapVariableCount).toList()

	val rowVariables: List<Int> = (submapVariableCount until submapVariableCount + rowVariableCount).toList()

	val columnVariables: List<Int> = (submapVariableCount + rowVariableCount until variableCount).toList()

	val cells: List<KarnaughCell> =
		(0 until submapCount).flatMap { s ->
			(0 until rowCount).flatMap { r -> (0 until columnCount).map { c -> KarnaughCell(s, r, c) } }
		}

	/** The truth table row represented by [cell]; truth table rows count in binary with input column 0 as MSB. */
	fun truthTableRow(cell: KarnaughCell): Int =
		(grayCode(cell.submap) shl (rowVariableCount + columnVariableCount)) or
			(grayCode(cell.row) shl columnVariableCount) or
			grayCode(cell.column)

	fun cellOf(truthTableRow: Int): KarnaughCell {
		require(truthTableRow in 0 until (1 shl variableCount)) { "Invalid truth table row: $truthTableRow" }
		return KarnaughCell(
			grayIndex(truthTableRow shr (rowVariableCount + columnVariableCount)),
			grayIndex((truthTableRow shr columnVariableCount) and (rowCount - 1)),
			grayIndex(truthTableRow and (columnCount - 1)),
		)
	}

	/**
	 * Returns the rectangles covered by a product [term] of a [DNF], using the literal convention of
	 * [TruthTable.getMinTerm]: literal `i` requires input column `i - 1` to be 0, literal `-i` requires it to be 1.
	 * A term wrapping around an edge is split into one rectangle per contiguous block.
	 */
	fun rectangles(term: List<Literal>): List<KarnaughRectangle> {
		val required = IntArray(variableCount) { -1 }
		term.forEach { required[abs(it) - 1] = if (it > 0) 0 else 1 }

		val rowRuns = runs(rowCount, rowVariables, required)
		val columnRuns = runs(columnCount, columnVariables, required)
		return (0 until submapCount)
			.filter { matches(grayCode(it), submapVariables, required) }
			.flatMap { submap ->
				rowRuns.flatMap { rows ->
					columnRuns.map { columns ->
						KarnaughRectangle(submap, rows.first, columns.first, rows.count(), columns.count())
					}
				}
			}
	}

	private fun matches(code: Int, variables: List<Int>, required: IntArray): Boolean =
		variables.withIndex().all { (i, variable) ->
			required[variable] < 0 || (code shr (variables.size - 1 - i)) and 1 == required[variable]
		}

	/** Groups the Gray-ordered indices matching [required] into maximal contiguous ranges. */
	private fun runs(count: Int, variables: List<Int>, required: IntArray): List<IntRange> {
		val runs = mutableListOf<IntRange>()
		var start = -1
		for (i in 0..count) {
			val match = i < count && matches(grayCode(i), variables, required)
			if (match && start < 0) {
				start = i
			} else if (!match && start >= 0) {
				runs.add(start until i)
				start = -1
			}
		}
		return runs
	}
}
