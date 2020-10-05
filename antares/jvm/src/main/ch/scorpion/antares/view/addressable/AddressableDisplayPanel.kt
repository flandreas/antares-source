package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.AddressableCellChange
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.RowHeaderTable
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import java.awt.*
import javax.swing.*
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.text.JTextComponent

/**
 * Displays the value of the individual cells of an [Addressable] using a [JTable].
 */
class AddressableDisplayPanel(
	private val addressable: Addressable,
	editable: Boolean,
	private val scheduler: Scheduler = ExecutionModule.scheduler
) : JPanel() {

	companion object {
		private val LOG by logger(AddressableDisplayPanel::class)
	}

	private val layouts = arrayOf<AddressableDisplayLayout>(
		FixedWidthLayout(1, addressable, editable),
		FixedWidthLayout(4, addressable, editable),
		FixedWidthLayout(8, addressable, editable),
		FixedWidthLayout(16, addressable, editable)
	)

    private val table = JTable(layouts[1].createTableModel())
	private val scrollPane = JScrollPane(table)
	private val layoutComboBox = JComboBox(layouts)
	private val addressableDisplayLayout: AddressableDisplayLayout get() = layoutComboBox.selectedItem as AddressableDisplayLayout
	private val changeCollector = ChangeCollector()

	val changes: Collection<AddressableCellChange> get() {
		return changeCollector.changes.values
	}

    init {
        buildUI()
	    layoutComboBox.addActionListener { updateMemoryDisplayLayout(addressableDisplayLayout) }
	    updateMemoryDisplayLayout(addressableDisplayLayout)
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

	private fun updateMemoryDisplayLayout(addressableDisplayLayout: AddressableDisplayLayout) {
		table.model.removeTableModelListener(changeCollector)
		table.model = addressableDisplayLayout.createTableModel()
		table.model.addTableModelListener(changeCollector)

		val rowHeaderTable = RowHeaderTable(table) { BitOperation.longToHexPadded(it.toLong() * addressableDisplayLayout.cellsPerRow, addressable.addressWidth) }
		scrollPane.setRowHeaderView(rowHeaderTable)
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.tableHeader)

		table.columnModel.columns.asSequence().forEach {
			val tableCellRenderer = AddressableCellRenderer()
			tableCellRenderer.horizontalAlignment = addressableDisplayLayout.columnAlignment(it.modelIndex)
			it.cellRenderer = tableCellRenderer

			val textField = JTextField()
			textField.horizontalAlignment = SwingConstants.RIGHT
			textField.inputVerifier = HexNumberInputVerifier(addressable.dataWidth)
			textField.font = table.font
			it.cellEditor = DefaultCellEditor(textField)
		}
	}

	private inner class AddressableCellRenderer : DefaultTableCellRenderer() {

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

			if (scheduler.isActive && addressable.isSelected && addressableDisplayLayout.getCellAddress(row, column) == addressable.currentAddress) {
				component.background = Graphics2DJvm.toAwtColor(Themes.get<EditTheme>().selection.color.foregroundColor)
			} else {
				if (isSelected) {
					component.background = table!!.selectionBackground
				} else {
					component.background = table!!.background
				}
			}
			return component
		}
	}

	private class HexNumberInputVerifier(private val bitWidth: BitWidth) : InputVerifier() {

		override fun verify(input: JComponent?): Boolean {
			val text = (input as JTextComponent).text
			val result = BitOperation.normalizeHex(text.trim(), bitWidth) != null
			LOG.debug("verifying '$text': $result")
			return result
		}
	}

	private inner class ChangeCollector : TableModelListener {

		/** Maps a memory address to the corresponding value change.*/
		val changes = mutableMapOf<Int, AddressableCellChange>()

		override fun tableChanged(e: TableModelEvent?) {
			if (e is AddressableTableModelEvent) {
				LOG.debug("cell at ${e.firstRow},${e.column} changed")
				val address = addressableDisplayLayout.getCellAddress(e.firstRow, e.column)
				val newValue = getCurrentValue(e.firstRow, e.column)
				changes[address] = AddressableCellChange(address, newValue, getOrigValue(address, e.oldValue))
			}
		}

		private fun getOrigValue(address: Int, previousValue: Long): Long {
			return changes[address]?.origValue ?: previousValue
		}

		private fun getCurrentValue(rowIndex: Int, columnIndex: Int): Long {
			return addressable.dataAt(addressableDisplayLayout.getCellAddress(rowIndex, columnIndex))
		}
	}
}