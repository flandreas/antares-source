package ch.scorpion.antares.view.memory

import ch.scorpion.jabbah.base.Math
import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.swing.RowHeaderTable
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.AbstractTableModel

/**
 * Displays the value of the individual cells of a [Memory].
 */
class MemoryDisplayPanel(
    private val memory: Memory,
    addressBitWidth: BitWidth,
    dataWidth: BitWidth,
    private val cellsPerRow: Int = DEFAULT_CELLS_PER_ROW
) : JPanel() {

    companion object {
        private val DEFAULT_CELLS_PER_ROW = 16
    }

    private val rowCount: Int = Math.ceil((addressBitWidth.power() / cellsPerRow).toDouble()).toInt()
    private val mask: Int = BitOperation.power(dataWidth.width.toLong()) - 1
    private val format = "%${Math.max(2, dataWidth.width / 4)}s"
    private val table = JTable(MemoryTableModel())

    init {
        buildUI()
    }

    private fun buildUI() {
        layout = BorderLayout()
        val scrollPane = JScrollPane(table)
        val rowHeaderTable = RowHeaderTable(table, { Integer.toHexString(it * cellsPerRow).toUpperCase()})
        scrollPane.setRowHeaderView(rowHeaderTable)
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.getTableHeader())
        add(scrollPane, BorderLayout.CENTER)
    }

    private inner class MemoryTableModel : AbstractTableModel() {

        /** ---- [AbstractTableModel] */

        override fun getRowCount(): Int {
            return this@MemoryDisplayPanel.rowCount
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
}