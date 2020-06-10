package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Addressable
import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import javax.swing.JLabel
import javax.swing.event.TableModelEvent
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableModel

interface MemoryDisplayLayout {

	val cellsPerRow: Int

	/** Creates a [TableModel] that displays the specified [Memory] contents according to this specific layout. */
	fun createTableModel(): TableModel

	/** Returns the text alignment of the specified column in terms of [JLabel.RIGHT] or [JLabel.LEFT].*/
	fun columnAlignment(columnIndex: Int): Int

	fun getCellAddress(rowIndex: Int, columnIndex: Int): Int
}

class MemoryTableModelEvent(
	source: TableModel,
	row: Int,
	column: Int,
	val oldValue: Long
) : TableModelEvent(source, row, column, TableModelEvent.UPDATE)

abstract class AbstractMemoryDisplayLayout(
	protected val addressable: Addressable
) : MemoryDisplayLayout

class FixedWidthLayout(
	override val cellsPerRow: Int,
	addressable: Addressable,
	private val editable: Boolean
) : AbstractMemoryDisplayLayout(addressable) {

	private val rowCount: Int = Math.ceil((addressable.addressWidth.power() / cellsPerRow).toDouble()).toInt()

	override fun toString(): String {
		return Translations.getString("antares.memory.layout.columns", cellsPerRow)
	}

	override fun getCellAddress(rowIndex: Int, columnIndex: Int): Int {
		return when (cellsPerRow) {
			1 -> rowIndex
			else -> rowIndex * cellsPerRow + columnIndex
		}
	}

	override fun createTableModel(): TableModel {
		return when (cellsPerRow) {
			1 -> SingleColumnTableModel(addressable, rowCount, editable)
			else -> MemoryTableModel(cellsPerRow, addressable, rowCount, editable)
		}
	}

	override fun columnAlignment(columnIndex: Int): Int {
		return when (cellsPerRow) {
			1 -> if (addressable.disassemblyWidth > 0) {
				when (columnIndex) {
					0 -> JLabel.RIGHT
					1 -> JLabel.LEFT
					2 -> JLabel.LEFT
					else -> throw IllegalArgumentException()
				}
			} else {
				when (columnIndex) {
					0 -> JLabel.RIGHT
					1 -> JLabel.LEFT
					else -> throw IllegalArgumentException()
				}
			}
			else -> JLabel.RIGHT
		}
	}
}

private abstract class AbstractMemoryTableModel(
	protected val cellsPerRow: Int,
	protected val addressable: Addressable,
	private val rowCount: Int
) : AbstractTableModel() {

	private val format: String = "%${Math.max(2, addressable.dataWidth.width / 4)}s"

	override fun getRowCount(): Int {
		return rowCount
	}

	override fun getColumnCount(): Int {
		return cellsPerRow
	}

	override fun getColumnName(column: Int): String {
		return Integer.toHexString(column).toUpperCase()
	}

	protected fun getMemoryValue(rowIndex: Int, columnIndex: Int): String {
		return BitOperation.longToHexPadded(getCellValue(rowIndex, columnIndex), addressable.dataWidth)
	}

	protected fun getCellAddress(rowIndex: Int, columnIndex: Int): Int {
		return rowIndex * cellsPerRow + columnIndex
	}

	protected fun getCellValue(rowIndex: Int, columnIndex: Int): Long {
		return addressable.dataAt(getCellAddress(rowIndex, columnIndex))
	}
}

private open class MemoryTableModel(
	cellsPerRow: Int,
	addressable: Addressable,
	rowCount: Int,
	private val editable: Boolean
) : AbstractMemoryTableModel(cellsPerRow, addressable, rowCount) {

	/** ---- [AbstractTableModel] */

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
		return getMemoryValue(rowIndex, columnIndex)
	}

	override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
		val oldValue = getCellValue(rowIndex, columnIndex)
		setMemoryValue(aValue as String, rowIndex, columnIndex)
		fireTableChanged(MemoryTableModelEvent(this, rowIndex, columnIndex, oldValue))
	}

	override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
		return editable
	}

	private fun setMemoryValue(value: String, rowIndex: Int, columnIndex: Int) {
		try {
			addressable.setDataAt(getCellAddress(rowIndex, columnIndex), BitOperation.hexToLong(value.trim()))
		} catch (e: IllegalArgumentException) {
			// empty
		}
	}
}

private class SingleColumnTableModel(
	addressable: Addressable,
	rowCount: Int,
	editable: Boolean
) : MemoryTableModel(1, addressable, rowCount, editable) {

	private val showDisassembly: Boolean = addressable.disassemblyWidth > 0
	private val valueColumnName = Translations.getString("antares.memory.layout.value")
	private val commentsColumnName = Translations.getString("antares.memory.layout.comment")
	private val disassemblyColumnName = Translations.getString("antares.memory.layout.disassembly")

	override fun getColumnCount(): Int {
		return if (showDisassembly) 3 else 2
	}

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
		return when (columnIndex) {
			0 -> getMemoryValue(rowIndex, columnIndex)
			1 -> if (showDisassembly) {
				addressable.disassemblyAt(rowIndex)
			} else {
				addressable.commentAt(rowIndex)
			}
			else -> addressable.commentAt(rowIndex)
		}
	}

	override fun getColumnName(column: Int): String {
		return when (column) {
			0 -> valueColumnName
			1 -> if (showDisassembly) disassemblyColumnName else commentsColumnName
			2 -> commentsColumnName
			else -> throw IllegalArgumentException("too many columns")
		}
	}
}
