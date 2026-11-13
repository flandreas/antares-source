package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.jabbah.io.*

abstract class AbstractTruthTableColumn(
	var name: String = ""
) : AbstractStorable() {

	open var rowsCount: Int = 0

	abstract fun getValue(row: Int): Bit

	abstract fun setValue(row: Int, value: Bit)

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
) : AbstractTruthTableColumn(name) {

	internal var bitIndex: Int = 0

	/** Input cells are a binary counter; storing them duplicates the row index. */
	override fun getValue(row: Int): Bit {
		require(row in 0 until rowsCount) { "Invalid truth table row: $row" }
		return Bit.of((row ushr bitIndex) and 1)
	}

	override fun setValue(row: Int, value: Bit) {
		throw IllegalArgumentException("Cannot set input column")
	}
}

/** Implements special logic for writing and reading user-editable cell values. */
class TruthTableOutputColumn(
	name: String = "",
) : AbstractTruthTableColumn(name) {

	// One byte per output instead of one object reference; input columns need no storage.
	private var values = ByteArray(0)

	override var rowsCount: Int = 0
		set(value) {
			require(value >= 0)
			if (field != value) {
				values = ByteArray(value)
				field = value
			}
		}

	override fun getValue(row: Int): Bit = when (values[row].toInt()) {
		0 -> Bit.False
		1 -> Bit.True
		2 -> Bit.Error
		else -> Bit.Undefined
	}

	override fun setValue(row: Int, value: Bit) {
		values[row] = when (value) {
			Bit.False -> 0
			Bit.True -> 1
			Bit.Error -> 2
			Bit.Undefined -> 3
		}
	}

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
		rowsCount = valuesString.length
		valuesString.forEachIndexed { row, value -> setValue(row, Bit.of(value)) }
	}
}
