package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.AddressableReference
import ch.scorpion.antares.model.addressable.Memory
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import java.util.*
import javax.swing.JLabel
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableModel
import kotlin.math.ceil
import kotlin.math.max

/**
 * Specified how the individual cells of an [Addressable] are to be represented as a [TableModel],
 * by for example specifying how many columns are to be displayed.
 **/
interface AddressableDisplayLayout {

	val cellsPerRow: Int

	/** Creates a [TableModel] that displays the specified [Memory] contents according to this specific layout. */
	fun createTableModel(): AbstractAddressableTableModel

	/** Returns the text alignment of the specified column in terms of [JLabel.RIGHT] or [JLabel.LEFT].*/
	fun columnAlignment(columnIndex: Int): Int

	fun getCellAddress(rowIndex: Int, columnIndex: Int): Int
}

abstract class AbstractAddressableDisplayLayout(
	protected val addressableRef: AddressableReference
) : AddressableDisplayLayout

class FixedWidthLayout(
	override val cellsPerRow: Int,
	addressable: AddressableReference,
	private val editable: () -> Boolean,
	private val signalHandler: SignalHandler? = null
) : AbstractAddressableDisplayLayout(addressable) {

	private val rowCount: Int = ceil(((addressableRef.addressable.addressWidth.maxValue + 1UL) / cellsPerRow.toULong()).toDouble()).toInt()

	override fun toString(): String =
		Translations.getString("antares.memory.layout.columns", cellsPerRow)

	override fun getCellAddress(rowIndex: Int, columnIndex: Int): Int =
		when (cellsPerRow) {
			1 -> rowIndex
			else -> rowIndex * cellsPerRow + columnIndex
		}

	override fun createTableModel(): AbstractAddressableTableModel =
		when (cellsPerRow) {
			1 -> SingleColumnTableModel(addressableRef, rowCount, editable, signalHandler)
			else -> AddressableTableModel(cellsPerRow, addressableRef, rowCount, editable, signalHandler)
		}

	override fun columnAlignment(columnIndex: Int): Int =
		when (cellsPerRow) {
			1 -> if (addressableRef.addressable.disassemblyWidth > 0) {
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

/**
 * Wraps an [Addressable] as a [TableModel] for displaying and editing.
 */
abstract class AbstractAddressableTableModel(
	private val cellsPerRow: Int,
	protected val addressableRef: AddressableReference,
	private val rowCount: Int
) : AbstractTableModel() {

	private val format: String = "%${max(2, addressableRef.addressable.dataWidth.width / 4)}s"

	abstract fun isCommentColumn(column: Int): Boolean

	override fun getRowCount(): Int = rowCount

	override fun getColumnCount(): Int = cellsPerRow

	override fun getColumnName(column: Int): String =
		Integer.toHexString(column).uppercase(Locale.getDefault())

	protected fun getMemoryValue(rowIndex: Int, columnIndex: Int): String =
		BitOperation.longToHexPadded(getCellValue(rowIndex, columnIndex), addressableRef.addressable.dataWidth)

	protected open fun getCellAddress(rowIndex: Int, columnIndex: Int): Int = rowIndex * cellsPerRow + columnIndex

	private fun getCellValue(rowIndex: Int, columnIndex: Int): ULong =
		addressableRef.addressable.dataAt(getCellAddress(rowIndex, columnIndex))

	protected fun getComment(rowIndex: Int, columnIndex: Int): String? =
		addressableRef.addressable.commentAt(getCellAddress(rowIndex, columnIndex))

	private fun rowOf(address: Int): Int = address / cellsPerRow

	private fun columnOf(address: Int): Int = address.mod(cellsPerRow)
}

private open class AddressableTableModel(
	cellsPerRow: Int,
	addressable: AddressableReference,
	rowCount: Int,
	private val editable: () -> Boolean,
	private val signalHandler: SignalHandler? = null
) : AbstractAddressableTableModel(cellsPerRow, addressable, rowCount) {

	/** ---- [AbstractTableModel] */

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
		return if (isCommentColumn(columnIndex)) {
			getComment(rowIndex, columnIndex)
		} else {
			getMemoryValue(rowIndex, columnIndex)
		}
	}

	override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
		if (isCommentColumn(columnIndex)) {
			setComment(aValue as String?, rowIndex, columnIndex)
		} else {
			setMemoryValue(aValue as String, rowIndex, columnIndex)
		}
	}

	override fun isCommentColumn(column: Int): Boolean = false

	override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = editable()

	protected fun setMemoryValue(value: String, rowIndex: Int, columnIndex: Int) {
		try {
			BitOperation.normalizeHex(value.trim(), addressableRef.addressable.dataWidth)?.let {
				addressableRef.addressable.setDataAt(getCellAddress(rowIndex, columnIndex), BitOperation.hexToLong(it), signalHandler)
			}
		} catch (e: IllegalArgumentException) {
			// empty
		}
	}

	protected fun setComment(value: String?, rowIndex: Int, columnIndex: Int) {
		addressableRef.addressable.setCommentAt(getCellAddress(rowIndex, columnIndex), value, signalHandler)
	}
}

private class SingleColumnTableModel(
	addressable: AddressableReference,
	rowCount: Int,
	editable: () -> Boolean,
	signalHandler: SignalHandler? = null
) : AddressableTableModel(1, addressable, rowCount, editable, signalHandler) {

	private val showDisassembly: Boolean = addressableRef.addressable.disassemblyWidth > 0
	private val valueColumnName = Translations.getString("antares.memory.layout.value")
	private val commentsColumnName = Translations.getString("antares.memory.layout.comment")
	private val disassemblyColumnName = Translations.getString("antares.memory.layout.disassembly")

	override fun getCellAddress(rowIndex: Int, columnIndex: Int): Int = rowIndex

	override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean =
		super.isCellEditable(rowIndex, columnIndex) && (columnIndex == 0 || columnIndex == 1 && !showDisassembly)

	override fun getColumnCount(): Int = if (showDisassembly) 3 else 2

	override fun isCommentColumn(column: Int): Boolean = column == 1 && !showDisassembly

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? =
		when (columnIndex) {
			0 -> getMemoryValue(rowIndex, columnIndex)
			1 -> if (showDisassembly) {
				addressableRef.addressable.disassemblyAt(rowIndex)
			} else {
				addressableRef.addressable.commentAt(rowIndex)
			}
			else -> addressableRef.addressable.commentAt(rowIndex)
	}

	override fun getColumnName(column: Int): String =
		when (column) {
			0 -> valueColumnName
			1 -> if (showDisassembly) disassemblyColumnName else commentsColumnName
			2 -> commentsColumnName
			else -> throw IllegalArgumentException("too many columns")
	}
}
