package ch.scorpion.antares.view.truthtable

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.truthtable.TruthTableCommand
import ch.scorpion.antares.model.truthtable.TruthTableReference
import ch.scorpion.jabbah.base.swing.RowHeaderTable
import ch.scorpion.jabbah.edit.CommandManager
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer

class TruthTableTableView(
	private val ref: TruthTableReference,
	private val commandManager: CommandManager,
	private val editable: Boolean = true
) : JPanel() {

	companion object {
		private const val CELL_FONT_SIZE = 18
		private const val COLUMN_WIDTH = 40

		private val FOREGROUND = UIManager.getColor("TextField.foreground")
		private val BACKGROUND = UIManager.getColor("TextField.background")
		private val INACTIVE_FOREGROUND = UIManager.getColor("TextField.inactiveForeground")
		private val INACTIVE_BACKGROUND = UIManager.getColor("TextField.inactiveBackground")
	}

	private var tableModel = TruthTableTableModel(ref, editable)

	private val table = JTable(tableModel)

	private val scrollPane = JScrollPane(table)

	private val cellFont = table.font.deriveFont(CELL_FONT_SIZE.toFloat())

	init {
		buildUI()
		updateColumnModels()
		if (editable) {
			defineSetActions()
		}
	}

	fun reloadModel() {
		tableModel.dispose()
		tableModel = TruthTableTableModel(ref, editable)
		table.model = tableModel
		updateColumnModels()
		updateRowHeaders()
	}

	private fun buildUI() {
		layout = BorderLayout()

		table.font = cellFont
		table.rowHeight = CELL_FONT_SIZE + 8
		table.autoResizeMode = JTable.AUTO_RESIZE_OFF
		table.setShowGrid(true)
		table.cellSelectionEnabled = true
		table.rowMargin = 1
		table.columnModel.columnMargin = 1

		scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED

		updateRowHeaders()

		add(scrollPane, BorderLayout.CENTER)
	}

	private fun updateRowHeaders() {
		val rowHeaderTable = RowHeaderTable(table) { it.toString() }
		scrollPane.setRowHeaderView(rowHeaderTable)
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.tableHeader)
	}

	private fun defineSetActions() {
		table.inputMap.put(KeyStroke.getKeyStroke("0"), "Action0")
		table.actionMap.put("Action0", SetAction(Bit.False))
		table.inputMap.put(KeyStroke.getKeyStroke("1"), "Action1")
		table.actionMap.put("Action1", SetAction(Bit.True))
		table.inputMap.put(KeyStroke.getKeyStroke("X"), "ActionX")
		table.actionMap.put("ActionX", SetAction(Bit.Error))
	}

	private fun updateColumnModels() {
		table.columnModel.columns.asSequence().forEachIndexed { index, tableColumn ->
			if (index < ref.truthTable.inputColumnCount) {
				tableColumn.cellRenderer = InputCellRenderer()
			} else {
				tableColumn.cellRenderer = OutputCellRenderer()
				tableColumn.cellEditor = OutputCellEditor(createOutputEditor())
			}
			tableColumn.preferredWidth = COLUMN_WIDTH
		}
	}

	private fun createOutputEditor(): JTextField {
		val textField = JTextField()
		textField.font = cellFont
		textField.isEditable = false
		return textField
	}

	private fun applyZebra(label: JLabel, row: Int, output: Boolean) {
		label.foreground = if (output) FOREGROUND else INACTIVE_FOREGROUND
		if (row.and(4) == 0) {
			label.background = BACKGROUND
		} else {
			label.background = INACTIVE_BACKGROUND
		}
	}

	private inner class InputCellRenderer : DefaultTableCellRenderer() {

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
			label.horizontalAlignment = SwingConstants.CENTER
			label.verticalAlignment = SwingConstants.CENTER
			label.font = cellFont

			if (!isSelected && !hasFocus) {
				applyZebra(label, row, output = false)
			}

			return label
		}
	}

	private inner class OutputCellEditor(textField: JTextField) : DefaultCellEditor(textField) {
		override fun isCellEditable(anEvent: EventObject?): Boolean = false
	}

	private inner class OutputCellRenderer : DefaultTableCellRenderer() {

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
			label.horizontalAlignment = SwingConstants.CENTER
			label.font = cellFont

			if (!isSelected && !hasFocus) {
				applyZebra(label, row, output = true)
			}

			return label
		}
	}

	private inner class SetAction(private val bit: Bit) : AbstractAction() {

		override fun actionPerformed(e: ActionEvent?) {
			val row = table.selectedRow
			val column = table.selectedColumn

			if (column in ref.truthTable.inputColumnCount until table.columnCount) {
				commandManager.execute(TruthTableCommand(ref, row, column, bit))
			}

			forwardSelection(row, column)
		}

		private fun forwardSelection(row: Int, column: Int) {
			var r = row
			var c = column

			c++
			if (c >= table.columnCount) {
				c = ref.truthTable.inputColumnCount
				r++
				if (r >= ref.truthTable.rowsCount) {
					r = 0
				}
			}
			table.changeSelection(r, c, false, false)
		}
	}
}