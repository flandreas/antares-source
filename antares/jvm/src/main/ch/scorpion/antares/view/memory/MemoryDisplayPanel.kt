package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Addressable
import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.RowHeaderTable
import com.l2fprod.common.swing.renderer.DefaultCellRenderer
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer

/**
 * Displays the value of the individual cells of a [Memory].
 */
class MemoryDisplayPanel(
	addressable: Addressable
) : JPanel() {

	private val layouts = arrayOf<MemoryDisplayLayout>(
		FixedWidthLayout(1, addressable),
		FixedWidthLayout(4, addressable),
		FixedWidthLayout(8, addressable),
		FixedWidthLayout(16, addressable)
	)

    private val table = JTable(layouts[1].createTableModel())
	private val scrollPane = JScrollPane(table)
	private val layoutComboBox = JComboBox<MemoryDisplayLayout>(layouts)
	private val memoryDisplayLayout: MemoryDisplayLayout get() = layoutComboBox.selectedItem as MemoryDisplayLayout

    init {
        buildUI()
	    layoutComboBox.addActionListener { updateMemoryDisplayLayout(memoryDisplayLayout) }
	    updateMemoryDisplayLayout(memoryDisplayLayout)
    }

	fun refresh() {
		table.invalidate()
		table.revalidate()
		table.repaint()
	}

    private fun buildUI() {
        layout = BorderLayout()

	    table.font = Font("Monospaced", Font.PLAIN, table.font.size)
	    table.tableHeader.reorderingAllowed = false
	    table.autoResizeMode = JTable.AUTO_RESIZE_OFF

	    val memoryLayoutPanel = JPanel(FlowLayout(FlowLayout.LEFT))
	    memoryLayoutPanel.add(JLabel(Translations.getString("antares.memory.layout.selector.name")))
	    memoryLayoutPanel.add(layoutComboBox)

	    add(memoryLayoutPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }

	private fun updateMemoryDisplayLayout(memoryDisplayLayout: MemoryDisplayLayout) {
		table.model = memoryDisplayLayout.createTableModel()

		val rowHeaderTable = RowHeaderTable(table) { Integer.toHexString(it * memoryDisplayLayout.cellsPerRow).toUpperCase() }
		scrollPane.setRowHeaderView(rowHeaderTable)
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.tableHeader)

		table.columnModel.columns.asSequence().forEach {
			val tableCellRenderer = DefaultTableCellRenderer()
			tableCellRenderer.horizontalAlignment = memoryDisplayLayout.columnAlignment(it.modelIndex)
			it.cellRenderer = tableCellRenderer
		}

		table.tableHeader.defaultRenderer = HeaderRenderer(table)
	}

	/** Establishes right-aligned column headers.*/
	private inner class HeaderRenderer(table: JTable) : TableCellRenderer {

		private val renderers: MutableList<DefaultTableCellRenderer> = mutableListOf()

		init {
			for (column in 0 until table.model.columnCount) {
				val renderer = DefaultCellRenderer()
				renderer.horizontalAlignment = memoryDisplayLayout.columnAlignment(column)
				renderers.add(renderer)
			}
		}

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			return renderers[column].getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
		}
	}
}