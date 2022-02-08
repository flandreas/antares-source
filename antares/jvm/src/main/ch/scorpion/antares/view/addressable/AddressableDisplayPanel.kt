package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.AddressableCellChange
import ch.scorpion.antares.model.addressable.AddressableCellChangeCommand
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.FocusJTable
import ch.scorpion.jabbah.base.swing.RowHeaderTable
import ch.scorpion.jabbah.base.swing.SelectAllCellEditor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.text.JTextComponent

/**
 * Displays the value of the individual cells of an [Addressable] using a [JTable].
 *
 * Allows the user to choose from different table layouts.
 * Creates [AddressableCellChangeCommand]s when the user manually edits cell.
 */
class AddressableDisplayPanel(
	private val addressableRef: AddressableReference,
	editable: () -> Boolean,
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val view: DrawingView<GraphView>,
	private val cmdManager: CommandManager = EditModule.commandManager
) : JPanel() {

	companion object {
		private val LOG by logger(AddressableDisplayPanel::class)
	}

	private val layouts = arrayOf<AddressableDisplayLayout>(
		FixedWidthLayout(1, addressableRef, editable, applicationContextHolder.scheduler),
		FixedWidthLayout(4, addressableRef, editable, applicationContextHolder.scheduler),
		FixedWidthLayout(8, addressableRef, editable, applicationContextHolder.scheduler),
		FixedWidthLayout(16, addressableRef, editable, applicationContextHolder.scheduler)
	)

    private val table = FocusJTable(layouts[1].createTableModel())
	private val scrollPane = JScrollPane(table)
	private val layoutComboBox = JComboBox(layouts)
	private val addressableDisplayLayout: AddressableDisplayLayout get() = layoutComboBox.selectedItem as AddressableDisplayLayout
	private val changeCollector = ChangeCollector()

    init {
        buildUI()
	    layoutComboBox.addActionListener { updateMemoryDisplayLayout(addressableDisplayLayout) }
	    updateMemoryDisplayLayout(addressableDisplayLayout)
    }

	fun dispose() {
		disposeTableModel()
	}

	private fun disposeTableModel() {
		if (table.model is AbstractAddressableTableModel) {
			(table.model as AbstractAddressableTableModel).dispose()
		}
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
		disposeTableModel()
		table.model = addressableDisplayLayout.createTableModel()
		table.model.addTableModelListener(changeCollector)

		val rowHeaderTable = RowHeaderTable(table) {
			BitOperation.longToHexPadded(it.toULong() * addressableDisplayLayout.cellsPerRow.toUInt(), addressableRef.addressable.addressWidth)
		}
		scrollPane.setRowHeaderView(rowHeaderTable)
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.tableHeader)

		table.columnModel.columns.asSequence().forEach {
			val tableCellRenderer = AddressableCellRenderer()
			tableCellRenderer.horizontalAlignment = addressableDisplayLayout.columnAlignment(it.modelIndex)
			it.cellRenderer = tableCellRenderer

			val textField = JTextField()
			textField.horizontalAlignment = SwingConstants.RIGHT
			textField.inputVerifier = HexNumberInputVerifier()
			textField.font = table.font
			it.cellEditor = SelectAllCellEditor(textField)
		}
	}

	private inner class AddressableCellRenderer : DefaultTableCellRenderer() {

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

			if (applicationContextHolder.scheduler.isActive
				&& addressableRef.addressable.isSelected
				&& addressableDisplayLayout.getCellAddress(row, column) == addressableRef.addressable.currentAddress
			) {
				component.background = Graphics2DJvm.toAwtColor(Look.highlightWithSelectionColor)
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

	/** Holds the [Command] being created after manual cell editing.*/
	private var changeCommand: Command? = null

	private inner class HexNumberInputVerifier : InputVerifier() {

		override fun verify(input: JComponent?): Boolean {
			val text = (input as JTextComponent).text
			val result = BitOperation.normalizeHex(text.trim(), addressableRef.addressable.dataWidth) != null
			if (result) {
				changeCommand?.let {
					cmdManager.register(it)
					changeCommand == null
				}
			}
			return result
		}
	}

	private inner class ChangeCollector : TableModelListener {

		/** Maps a memory address to the corresponding value change.*/
		val changes = mutableMapOf<Int, AddressableCellChange>()

		override fun tableChanged(e: TableModelEvent?) {
			if (e is AddressableTableModelEvent) {
				LOG.trace("cell at ${e.firstRow},${e.column} changed")
				val address = addressableDisplayLayout.getCellAddress(e.firstRow, e.column)
				val newValue = getCurrentValue(e.firstRow, e.column)
				if (addressableRef.addressable.storesCells && newValue != e.oldValue) {
					changeCommand = AddressableCellChangeCommand(
						view,
						addressableRef.id,
						listOf(AddressableCellChange(address, newValue, getOrigValue(address, e.oldValue))))
				}
			}
		}

		private fun getOrigValue(address: Int, previousValue: ULong): ULong =
			changes[address]?.origValue ?: previousValue

		private fun getCurrentValue(rowIndex: Int, columnIndex: Int): ULong =
			addressableRef.addressable.dataAt(addressableDisplayLayout.getCellAddress(rowIndex, columnIndex))
	}
}