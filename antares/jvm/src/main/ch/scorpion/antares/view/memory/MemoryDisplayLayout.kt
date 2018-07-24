package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Addressable
import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.Translations
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableModel

interface MemoryDisplayLayout {

	val cellsPerRow: Int

	/** Creates a [TableModel] that displays the specified [Memory] contents according to this specific layout. */
	fun createTableModel(): TableModel
}

abstract class AbstractMemoryDisplayLayout(
	protected val addressable: Addressable
) : MemoryDisplayLayout

class FixedWidthLayout(
	override val cellsPerRow: Int,
	addressable: Addressable
) : AbstractMemoryDisplayLayout(addressable) {

	private val rowCount: Int = Math.ceil((addressable.addressWidth.power() / cellsPerRow).toDouble()).toInt()

	override fun toString(): String {
		return Translations.getString("antares.memory.layout.columns", cellsPerRow)
	}

	override fun createTableModel(): TableModel {
		return MemoryTableModel(cellsPerRow, addressable, rowCount)
	}
}

class MemoryTableModel(
	private val cellsPerRow: Int,
	private val addressable: Addressable,
	private val rowCount: Int,
	private val format: String = "%${Math.max(2, addressable.dataWidth.width / 4)}s"
) : AbstractTableModel() {

	private val mask: Int = BitOperation.power(addressable.dataWidth.width.toLong()) - 1

	private val showDisassembly: Boolean = cellsPerRow == 1 && addressable.disassemblyWidth > 0

	/** ---- [AbstractTableModel] */

	override fun getRowCount(): Int {
		return rowCount
	}

	override fun getColumnCount(): Int {
		return if (showDisassembly) 2 else cellsPerRow
	}

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
		if (showDisassembly && columnIndex == 1) {
			return addressable.disassemblyAt(rowIndex)
		}
		val address = rowIndex * cellsPerRow + columnIndex
		val value = addressable.dataAt(address)
		return String.format(format, java.lang.Long.toHexString(value and mask.toLong()).toUpperCase())
	}

	override fun getColumnName(column: Int): String {
		if (showDisassembly && column == 1) {
			return "Disassembly"
		}
		return Integer.toHexString(column).toUpperCase()
	}
}