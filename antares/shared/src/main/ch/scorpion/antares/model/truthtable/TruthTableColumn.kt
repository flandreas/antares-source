package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.jabbah.io.*

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