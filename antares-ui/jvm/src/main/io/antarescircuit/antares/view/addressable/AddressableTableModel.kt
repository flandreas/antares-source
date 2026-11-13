package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.AddressableReference
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableModel
import kotlin.math.max

/**
 * Wraps an [Addressable] as a [TableModel] for displaying and editing.
 */
abstract class AbstractAddressableTableModel(
    private val cellsPerRow: Int,
    protected val addressableRef: AddressableReference,
    private val rowCount: Int,
    protected val converterProvider: () -> AddressableValueConverter,
	protected val startAddress: Int = 0,
) : AbstractTableModel() {

    private val format: String = "%${max(2, addressableRef.addressable.dataWidth.width / 4)}s"

    abstract fun isCommentColumn(column: Int): Boolean

    override fun getRowCount(): Int = rowCount

    override fun getColumnCount(): Int = cellsPerRow

    override fun getColumnName(column: Int): String =
        converterProvider().render(column.toULong(), BitWidth.BW_4)

    protected fun getMemoryValue(rowIndex: Int, columnIndex: Int): String =
        converterProvider().render(getCellValue(rowIndex, columnIndex), addressableRef.addressable.dataWidth)

    protected open fun getCellAddress(rowIndex: Int, columnIndex: Int): Int =
		(startAddress.toLong() + rowIndex.toLong() * cellsPerRow + columnIndex).takeIf {
			it <= addressableRef.addressable.addressWidth.maxValue.coerceAtMost(Int.MAX_VALUE.toULong()).toLong()
		}?.toInt() ?: -1

	fun addressAt(rowIndex: Int, columnIndex: Int): Int = getCellAddress(rowIndex, columnIndex)

    private fun getCellValue(rowIndex: Int, columnIndex: Int): ULong =
        addressableRef.addressable.dataAt(getCellAddress(rowIndex, columnIndex))

    protected fun getComment(rowIndex: Int, columnIndex: Int): String? =
        addressableRef.addressable.commentAt(getCellAddress(rowIndex, columnIndex))

    private fun rowOf(address: Int): Int = address / cellsPerRow

    private fun columnOf(address: Int): Int = address.mod(cellsPerRow)
}

open class AddressableTableModel(
    cellsPerRow: Int,
    addressable: AddressableReference,
    rowCount: Int,
    private val editable: () -> Boolean,
    converterProvider: () -> AddressableValueConverter,
    private val signalHandler: SignalHandler? = null,
	startAddress: Int = 0,
) : AbstractAddressableTableModel(cellsPerRow, addressable, rowCount, converterProvider, startAddress) {

    /** ---- [AbstractTableModel] */

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return if (getCellAddress(rowIndex, columnIndex) < 0) {
			""
		} else if (isCommentColumn(columnIndex)) {
            getComment(rowIndex, columnIndex)
        } else {
            getMemoryValue(rowIndex, columnIndex)
        }
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (!isCellEditable(rowIndex, columnIndex)) return
		if (isCommentColumn(columnIndex)) {
            setComment(aValue as String?, rowIndex, columnIndex)
        } else {
            setMemoryValue(aValue as String, rowIndex, columnIndex)
        }
    }

    override fun isCommentColumn(column: Int): Boolean = false

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = editable() && getCellAddress(rowIndex, columnIndex) >= 0

    private fun setMemoryValue(value: String, rowIndex: Int, columnIndex: Int) {
        try {
            converterProvider().parse(value.trim(), addressableRef.addressable.dataWidth)?.let {
                addressableRef.addressable.setDataAt(getCellAddress(rowIndex, columnIndex), it, signalHandler)
            }
        } catch (e: IllegalArgumentException) {
            // empty
        }
    }

    private fun setComment(value: String?, rowIndex: Int, columnIndex: Int) {
        addressableRef.addressable.setCommentAt(getCellAddress(rowIndex, columnIndex), value, signalHandler)
    }
}

class SingleColumnTableModel(
    addressable: AddressableReference,
    rowCount: Int,
    editable: () -> Boolean,
    converterProvider: () -> AddressableValueConverter,
    signalHandler: SignalHandler? = null,
	startAddress: Int = 0,
) : AddressableTableModel(1, addressable, rowCount, editable, converterProvider, signalHandler, startAddress) {

    private val showDisassembly: Boolean = addressableRef.addressable.disassemblyWidth > 0
    private val valueColumnName = Translations.getString("antares.memory.layout.value")
    private val commentsColumnName = Translations.getString("antares.memory.layout.comment")
    private val disassemblyColumnName = Translations.getString("antares.memory.layout.disassembly")

    override fun getCellAddress(rowIndex: Int, columnIndex: Int): Int = super.getCellAddress(rowIndex, 0)

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean =
        super.isCellEditable(rowIndex, columnIndex) && (columnIndex == 0 || isCommentColumn(columnIndex))

    override fun getColumnCount(): Int = if (showDisassembly) 3 else 2

    override fun isCommentColumn(column: Int): Boolean = column == if (showDisassembly) 2 else 1

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? =
        if (getCellAddress(rowIndex, columnIndex) < 0) "" else when (columnIndex) {
            0 -> getMemoryValue(rowIndex, columnIndex)
            1 -> if (showDisassembly) {
                addressableRef.addressable.disassemblyAt(getCellAddress(rowIndex, 0))
            } else {
                addressableRef.addressable.commentAt(getCellAddress(rowIndex, 0))
            }
            else -> addressableRef.addressable.commentAt(getCellAddress(rowIndex, 0))
        }

    override fun getColumnName(column: Int): String =
        when (column) {
            0 -> valueColumnName
            1 -> if (showDisassembly) disassemblyColumnName else commentsColumnName
            2 -> commentsColumnName
            else -> throw IllegalArgumentException("too many columns")
        }
}