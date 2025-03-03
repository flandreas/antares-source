package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.quinemccluskey.MinTerm
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.base.collection.indexOfFirstOrNull
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.io.*

/**
 * The model of a user-editable truth table used for circuit synthesis.
 *
 * The data is organized in columns, where the input columns precede the output columns.
 * Accessing individual table cell values, which hold [Bits][Bit], is organized to suit a UI-oriented
 * table model, where row and column indices span the entire range of all columns.
 *
 * If used within a [TruthTableSavable], make sure that no one keeps references to [TruthTable]
 * without reacting to [ApplicationDataContentEvent] resulting from recovering from undoable snapshots.
 *
 * [TruthTable] uses [Bit.Error] to represent "any value".
 */
class TruthTable(
	initialName: String = "",
	inputColumnNames: List<String> = emptyList(),
	outputColumnNames: List<String> = emptyList(),
	stateColumnCount: Int = 0
) : AbstractStorable(), Namable, Describable, Bean {

	companion object {
		private val outputRegex = listOf(
			"^!?([a-zA-Z]\\w*)\$".toRegex(),
			"^!\\(([a-zA-Z]\\w*)\\)\$".toRegex()
		)
	}

	val inputColumnCount: Int get() = inputColumns.size

	val outputColumnCount: Int get() = outputColumns.size

	val rowsCount: Int get() = BitOperation.power(inputColumnCount.toByte()).toInt()

	val columnCount: Int get() = inputColumnCount + outputColumnCount

	val allInputNamesAreSingleChar: Boolean get() = inputColumns.all { it.name.length == 1 }

	val allNamesAreSingleChar: Boolean get() = allInputNamesAreSingleChar && outputColumns.all { it.name.length == 1}

	/**
	 * If [stateColumnCount] is > 0, the first [stateColumnCount] of the input columns represent Zn,
	 * and the first [stateColumnCount] of the output columns represent Zn+1 of a sequential system's truth table.
	 */
	var stateColumnCount: Int = stateColumnCount
		private set

	private val inputColumns: MutableList<TruthTableInputColumn> =
		inputColumnNames.map { TruthTableInputColumn(it) }.toMutableList()

	private val outputColumns: MutableList<TruthTableOutputColumn> =
		outputColumnNames.map { TruthTableOutputColumn(it) }.toMutableList()

	private val listeners = mutableListOf<TruthTableListener>()

	init {
		updateRowsCounts()
		fillInputCells()
	}

	override fun toString(): String = name.getTranslation()

	fun isStateColumn(column: Int): Boolean {
		return stateColumnCount > 0
			&& (column in 0 until stateColumnCount || column in inputColumnCount until inputColumnCount + stateColumnCount)
	}

	fun getValue(row: Int, column: Int): Bit = getColumn(column).getValue(row)

	fun setValue(row: Int, column: Int, value: Bit) {
		if (column < inputColumnCount) {
			throw IllegalArgumentException("Cannot set input column")
		}
		outputColumns[column - inputColumnCount].setValue(row, value)
		notifyListeners(row, column, value)
	}

	fun setColumnValues(column: Int, vararg values: Bit) {
		for (row in values.indices) {
			setValue(row, column, values[row])
		}
	}

	fun setColumnValue(column: Int, value: Bit) {
		for (row in 0 until rowsCount) {
			setValue(row, column, value)
		}
	}

	fun getColumnValues(column: Int): List<Bit> =
		(0 until rowsCount).map { getValue(it, column) }

	fun getColumnName(column: Int): String = getColumn(column).name

	fun addListener(l: TruthTableListener) {
		if (!listeners.contains(l)) {
			listeners.add(l)
		}
	}

	fun hasInputName(name: String): Boolean = inputColumns.any { it.name == name }

	fun hasOutputName(name: String): Boolean = outputColumns.any { it.name == name }

	fun getInputColumn(name: String): Int? =
		inputColumns.indexOfFirstOrNull { it.name == name }

	fun getMinTerms(outputColumn: Int): List<MinTerm> = getMinTerms(outputColumn, Bit.True)

	fun getDontCares(outputColumn: Int): List<MinTerm> = getMinTerms(outputColumn, Bit.Error)

	private fun getMinTerms(outputColumn: Int, bit: Bit): List<MinTerm> =
		(0 until rowsCount)
			.filter { getValue(it, outputColumn) == bit }
			.map { getMinTerm(it) }

	fun getMinTerm(row: Int): MinTerm =
		(inputColumnCount - 1 downTo 0)
			.mapIndexedNotNull { i, col ->
				getValue(row, col)
					.takeIf { bit -> bit == Bit.False }
					?.let { BitOperation.power(i.toByte()) } }
			.sum().toInt()

	fun removeListener(l: TruthTableListener) {
		listeners.remove(l)
	}

	fun getOutputColumnInfo(column: Int): TruthTableOutputColumnInfo {
		val columnName = getColumnName(column).trim()

		outputRegex.forEach {
			val result = it.matchEntire(columnName)
			if (result != null && result.groupValues.size > 1) {
				return TruthTableOutputColumnInfo(result.groupValues[1], columnName.startsWith('!'))
			}
		}

		return TruthTableOutputColumnInfo(columnName, false)
	}

	private fun notifyListeners(row: Int, column: Int, value: Bit) {
		val event = TruthTableEvent(this, row, column, value)
		listeners.forEach { it.dataChanged(event) }
	}

	private fun getColumn(columnIndex: Int): AbstractTruthTableColumn =
		if (columnIndex < inputColumnCount) {
			inputColumns[columnIndex]
		} else {
			outputColumns[columnIndex - inputColumnCount]
		}

	private fun updateRowsCounts() {
		inputColumns.forEach { it.rowsCount = rowsCount }
		outputColumns.forEach { it.rowsCount = rowsCount }
	}

	private fun fillInputCells() {
		for (row in 0 until rowsCount) {
			val value = DigitalSignalFactory.ofMinimalBitWidth(row.toULong())
			for (bitIndex in 0 until inputColumnCount ) {
				val bit = if (bitIndex > value.bitWidth.width - 1) Bit.False else value.bitAt(bitIndex)
				inputColumns[inputColumnCount - 1 - bitIndex].setValue(row, bit)
			}
		}
	}

	/** ---- [Namable] interface */

	override var name: Name by observableName(Name(initialName))

	/** ---- [Describable] interface */

	override var description: Description by observableDescription(Description(""))

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		name.write("name", writer)
		description.write("description", writer)
		writer.writeStorables("inputs", inputColumns.iterator())
		writer.writeStorables("outputs", outputColumns.iterator())
		if (stateColumnCount > 0) {
			writer.writeInt("stateColumnCount", stateColumnCount)
		}
	}

	override fun read(reader: StoreReader) {
		name = Name.read("name", reader)
		if (reader.hasElement("desc")) {
			description = Description.read("desc", reader)
		}
		inputColumns.clear()
		inputColumns.addAll(reader.readStorables("inputs"))

		outputColumns.clear()
		outputColumns.addAll(reader.readStorables("outputs"))

		if (reader.hasAttribute("stateColumnCount")) {
			stateColumnCount = reader.readInt("stateColumnCount")
		}

		updateRowsCounts()
		fillInputCells()
	}
}

/**
 * Used to extract negation information from a standard Antares port name of the form "!O" or "!(O).
 * @property plainName the column name without negation operator or parentheses
 * @property isNegated `true` if the original port name was negated
 */
data class TruthTableOutputColumnInfo(
	val plainName: String,
	val isNegated: Boolean
)

data class TruthTableEvent(
	val source: TruthTable,
	val row: Int,
	val column: Int,
	val newValue: Bit
)

fun interface TruthTableListener {
	fun dataChanged(event: TruthTableEvent)
}