package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.model.text.description.observableName
import ch.scorpion.jabbah.io.*

/**
 * The model of a user-editable truth table used for circuit synthesis.
 *
 * The data is organized in columns, where the input columns precede the output columns.
 * Accessing individual table cell values, which hold [Bits][Bit], is organized to suit a UI-oriented
 * table model, where row and column indices span the entire range of all columns.
 *
 * [TruthTable] uses [Bit.Error] to represent "any value".
 */
class TruthTable(
	initialName: String = "",
	inputColumnNames: List<String> = emptyList(),
	outputColumnNames: List<String> = emptyList()
) : AbstractStorable(), Namable {

	var uuid: UUID = System.createUUID()
		private set

	val inputColumnCount: Int get() = inputColumns.size

	val outputColumnCount: Int get() = outputColumns.size

	val rowsCount: Int get() = BitOperation.power(inputColumnCount.toByte()).toInt()

	private val inputColumns: MutableList<TruthTableInputColumn> =
		inputColumnNames.map { TruthTableInputColumn(it) }.toMutableList()

	private val outputColumns: MutableList<TruthTableOutputColumn> =
		outputColumnNames.map { TruthTableOutputColumn(it) }.toMutableList()

	init {
		updateRowsCounts()
		fillInputCells()
	}

	fun getValue(row: Int, column: Int): Bit = getColumn(column).getValue(row)

	fun setValue(row: Int, column: Int, value: Bit) {
		if (column < inputColumnCount) {
			throw IllegalArgumentException("Cannot set input column")
		}
		outputColumns[column - inputColumnCount].setValue(row, value)
	}

	fun getColumnName(column: Int): String = getColumn(column).name

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

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeString("uuid", uuid.toString())
		name.write("name", writer)
		writer.writeStorables("inputs", inputColumns.iterator())
		writer.writeStorables("outputs", outputColumns.iterator())
	}

	override fun read(reader: StoreReader) {
		uuid = UUID(reader.readString("uuid"))
		name = Name.read("name", reader)
		inputColumns.clear()
		inputColumns.addAll(reader.readStorables("inputs"))

		outputColumns.clear()
		outputColumns.addAll(reader.readStorables("outputs"))

		updateRowsCounts()
		fillInputCells()
	}
}

abstract class AbstractTruthTableColumn(
	var name: String = ""
) : AbstractStorable() {

	private var values: Array<Bit> = arrayOf()

	var rowsCount: Int = 0
		set(value) {
			if (field != value) {
				field = value
				values = Array(rowsCount) { Bit.False }
			}
		}

	fun getValue(row: Int): Bit = values[row]

	fun setValue(row: Int, value: Bit) {
		values[row] = value
	}

	override fun write(writer: StoreWriter) {
		writer.writeString("name", name)
	}

	override fun read(reader: StoreReader) {
		name = reader.readString("name")
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }
}

class TruthTableInputColumn(
	name: String = "",
) : AbstractTruthTableColumn(name)

/** Implements special logic for writing and reading user-editable cell values. */
class TruthTableOutputColumn(
	name: String = "",
) : AbstractTruthTableColumn(name) {

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("values", createValuesString())
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		parseValuesString(reader.readString("values"))
	}

	private fun createValuesString(): String {
		val buffer = StringBuilder()
		for (row in 0 until rowsCount) {
			buffer.append(getValue(row).toString())
		}
		return buffer.toString()
	}

	private fun parseValuesString(valuesString: String) {
		var row = 0
		val bits = mutableListOf<Bit>()
		for (c in valuesString) {
			bits.add(Bit.of(c))
			row++
		}

		rowsCount = bits.size
		bits.forEachIndexed { i, bit -> setValue(i, bit) }
	}
}