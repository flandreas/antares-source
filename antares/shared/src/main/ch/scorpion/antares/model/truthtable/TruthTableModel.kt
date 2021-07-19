package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.Vertice

typealias Bits = Array<Bit>

/**
 * A [TruthTableModel] documents all possible combinations of input [Bit]s and the corresponding output [Bit]s
 * of a digital [Vertice].
 */
class TruthTableModel(
	val inputColumns: List<Column>,
	val outputColumnNames: List<String>
) {

	constructor(inputCount: Int, outputCount: Int) : this(
		defaultInputColumns(inputCount),
		defaultOutputColumnNames(outputCount))

	companion object {
		fun defaultInputColumns(inputCount: Int): List<Column> = (0 until inputCount).map { Column(('A'.code + it).toChar().toString()) }
		fun defaultOutputColumnNames(outputCount: Int): List<String> = if (outputCount == 1) listOf("O") else (1..outputCount).map { "O$it" }
	}

	private val _rows: MutableList<Row> = mutableListOf()

	val inputCount: Int get() = inputColumns.size

	val outputCount: Int get() = outputColumnNames.size

	val rows: List<Row> get() = _rows

	init {
		predefineRows()
	}

	/**
	 * Defines a row for the specified input [Bit]s and the single output [Bit].
	 * Only applicable if [outputCount] is 1.
	 * @return this to support method chaining
	 * @throws IllegalStateException if [outputCount] is not 1
	 * @throws IllegalArgumentException if the size of [input] is not [inputCount]
	 */
	fun define(input: Bits, output: Bit): TruthTableModel {
		checkArgument(input.size == inputCount, "number of inputs must match inputCount of model")
		checkState(outputCount == 1, "can only be used if outputCount of model is 1")
		define(input, arrayOf(output))
		return this
	}

	/**
	 * Defines a row for the specified input and output [Bits].
	 * @return this to support method chaining
	 * @throws IllegalArgumentException if the sizes of [input] or [output] don't match the corresponding column numbers
	 */
	fun define(input: Bits, output: Bits): TruthTableModel {
		checkArgument(input.size == inputCount, "number of inputs must match inputCount of model")
		checkArgument(output.size == outputCount, "number of outputs must match outputCount of model")
		_rows[rowIndex(input)] = Row(input, output)
		return this
	}

	/**
	 * Convenience method for defining [TruthTableModel] for a single output with 0 and 1 instead of [Bits].
	 * @throws IllegalStateException if [outputCount] is not 1
	 * @return this to support method chaining
	 */
	fun define(input: IntArray, output: Int): TruthTableModel {
		return define(input, intArrayOf(output))
	}

	/**
	 * Convenience method for defining [TruthTableModel] with 0 and 1 instead of [Bits].
	 * @return this to support method chaining
	 * @throws IllegalArgumentException if the sizes of [input] or [output] don't match the corresponding column numbers
	 */
	fun define(input: IntArray, output: IntArray): TruthTableModel {
		return define(intsToBits(input), intsToBits(output))
	}

	/** Returns the output [Bits] for the specified input [Bits] combination.*/
	fun outputOf(input: Bits): Bits {
		return getRow(input).output
	}

	/** Convenience method for accessing _rows with 0 and 1 arrays instead of [Bits].*/
	fun outputOf(input: IntArray): IntArray {
		return bitsToInts(outputOf(intsToBits(input)))
	}

	/** Calculates the [TruthTableModel] by using [Bit] 0 of the specified [MultiSignalSource].*/
	fun calculate(calculator: (MultiSignalSource<DigitalSignal>) -> DigitalSignal): TruthTableModel {
		for (row in rows) {
			row.output[0] = calculator(row).bitAt(0)
		}
		return this
	}

	/** Creates and registers a [Row] with zero outputs for every possible input combination.*/
	private fun predefineRows() {
		(0UL until BitOperation.power(inputCount.toByte())).mapTo(_rows) {
			Row(
				Bit.listFromLong(it, inputCount).toTypedArray(),
				Bit.listFromInt(0, outputCount).toTypedArray()
			)
		}

	}

	private fun getRow(input: Bits): Row {
		return _rows.first { it.input contentDeepEquals input }
	}

	fun rowIndex(input: Bits): Int {
		return _rows.indexOfFirst { it.input contentDeepEquals input }
	}

	/**
	 * Converts an [Array] of 0 and 1 to the corresponding [Bits] array.
	 * The result contains [Bit.False] for every input number that is not 1.
	 */
	private fun intsToBits(ints: IntArray): Bits = Array(ints.size) { Bit.of(ints[it]) }

	/**
	 * Converts an [Array] of [Bit]s to the corresponding [Int] array with 0 and 1.
	 * @throws NullPointerException if a [Bit] is undefined
	 */
	private fun bitsToInts(bits: Bits): IntArray {
		return IntArray(bits.size) { bits[it].numericalValue }
	}

	inner class Row(val input: Bits, val output: Bits) : MultiSignalSource<DigitalSignal> {
		override val signalCount: Int get() = input.size
		override fun getSignal(id: Int): DigitalSignal = inputColumns[id - 1].logic.evaluate(input[id - 1].asWord)
	}

	data class Column(val name: String, val logic: Logic = Logic.POSITIVE)
}
