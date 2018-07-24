package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
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
	protected val memory: Memory,
	protected val addressBitWidth: BitWidth,
	protected val dataWidth: BitWidth
) : MemoryDisplayLayout

class FixedWidthLayout(
	override val cellsPerRow: Int,
	memory: Memory,
	addressBitWidth: BitWidth,
	dataWidth: BitWidth
) : AbstractMemoryDisplayLayout(memory, addressBitWidth, dataWidth) {

	private val rowCount: Int = Math.ceil((addressBitWidth.power() / cellsPerRow).toDouble()).toInt()

	override fun toString(): String {
		return Translations.getString("antares.memory.layout.columns", cellsPerRow)
	}

	override fun createTableModel(): TableModel {
		return MemoryTableModel(cellsPerRow, memory, dataWidth, rowCount)
	}
}

class MemoryTableModel(
	private val cellsPerRow: Int,
	private val memory: Memory,
	private val dataWidth: BitWidth,
	private val rowCount: Int,
	private val format: String = "%${Math.max(2, dataWidth.width / 4)}s"
) : AbstractTableModel() {

	private val mask: Int = BitOperation.power(dataWidth.width.toLong()) - 1

	/** ---- [AbstractTableModel] */

	override fun getRowCount(): Int {
		return rowCount
	}

	override fun getColumnCount(): Int {
		return cellsPerRow
	}

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
		val value = memory.read(rowIndex * cellsPerRow + columnIndex)
		return String.format(format, java.lang.Long.toHexString(value and mask.toLong()).toUpperCase())
	}

	override fun getColumnName(column: Int): String {
		return Integer.toHexString(column).toUpperCase()
	}
}