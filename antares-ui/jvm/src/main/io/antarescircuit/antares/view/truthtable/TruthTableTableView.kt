package io.antarescircuit.antares.view.truthtable

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.truthtable.TruthTableCellChange
import io.antarescircuit.antares.model.truthtable.TruthTableCellsCommand
import io.antarescircuit.antares.model.truthtable.TruthTableClipboard
import io.antarescircuit.antares.model.truthtable.TruthTableReference
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.base.swing.RowHeaderTable
import io.antarescircuit.jabbah.base.ui.Clipboard
import io.antarescircuit.jabbah.edit.CommandManager
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.datatransfer.UnsupportedFlavorException
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
		private const val COLUMN_WIDTH = 35

		private val FOREGROUND = UIManager.getColor("TextField.foreground")
		private val BACKGROUND = UIManager.getColor("TextField.background")
		private val INACTIVE_FOREGROUND = UIManager.getColor("TextField.inactiveForeground")
		private val INACTIVE_BACKGROUND = UIManager.getColor("TextField.inactiveBackground")
	}

	private var tableModel = TruthTableTableModel(ref, editable)

	val table = JTable(tableModel)

	private val scrollPane = JScrollPane(table)

	private val cellFont = table.font.deriveFont(CELL_FONT_SIZE.toFloat())

	init {
		buildUI()
		updateColumnModels()
		defineClipboardActions()
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
		table.inputMap.put(KeyStroke.getKeyStroke("shift X"), "ActionX")
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
			tableColumn.preferredWidth = COLUMN_WIDTH + 5 * ref.truthTable.getColumnName(index).length
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

	private fun defineClipboardActions() {
		val menu = JPopupMenu()
		val copy = object : AbstractAction(Translations.getString("edit.action.copy.name")) {
			override fun actionPerformed(e: ActionEvent?) {
				selectedText()?.let { Clipboard.setStringContents(it) }
			}
		}
		table.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Modifier.Meta.mask), "copyTruthTable")
		table.actionMap.put("copyTruthTable", copy)
		menu.add(copy)
		if (editable) {
			val paste = object : AbstractAction(Translations.getString("edit.action.paste.name")) {
				override fun actionPerformed(e: ActionEvent?) {
					try {
						Clipboard.getStringContents()?.let { pasteText(it) }
					} catch (_: IllegalArgumentException) {
						showPasteError()
					} catch (_: UnsupportedFlavorException) {
						showPasteError()
					}
				}
			}
			table.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Modifier.Meta.mask), "pasteTruthTable")
			table.actionMap.put("pasteTruthTable", paste)
			menu.add(paste)
			menu.addSeparator()
			for (bit in listOf(Bit.False, Bit.True, Bit.Error)) {
				menu.add(SetAction(bit))
			}
		}
		table.componentPopupMenu = menu
	}

	private fun showPasteError() {
		JOptionPane.showMessageDialog(this,
			Translations.getString("antares.truthTable.paste.error"),
			Translations.getString("edit.action.paste.name"), JOptionPane.ERROR_MESSAGE)
	}

	private fun selectedText(): String? {
		if (table.selectedRow < 0 || table.selectedColumn < 0) return null
		return table.selectedRows.joinToString("\n") { row ->
			table.selectedColumns.joinToString("\t") { column -> table.getValueAt(row, column).toString() }
		}
	}

	/** Validate the entire rectangle before creating a single undoable edit. */
	private fun pasteText(text: String) {
		if (!editable || table.selectedRow < 0 || table.selectedColumn < 0) return
		val values = TruthTableClipboard.parse(text)
		if (values.size == 1 && values[0].size == 1) {
			fillSelection(values[0][0])
			return
		}
		val firstRow = table.selectedRow
		val firstColumn = table.selectedColumn
		require(values.size <= table.rowCount - firstRow)
		require(values[0].size <= table.columnCount - firstColumn)
		val changes = values.flatMapIndexed { row, cells ->
			cells.mapIndexed { column, bit ->
				TruthTableCellChange(firstRow + row, table.convertColumnIndexToModel(firstColumn + column), bit)
			}
		}
		applyChanges(changes)
	}

	private fun fillSelection(bit: Bit) {
		val changes = table.selectedRows.flatMap { row ->
			table.selectedColumns.map { viewColumn ->
				val column = table.convertColumnIndexToModel(viewColumn)
				if (column < ref.truthTable.inputColumnCount) null else TruthTableCellChange(row, column, bit)
			}.filterNotNull()
		}
		applyChanges(changes)
	}

	private fun applyChanges(changes: List<TruthTableCellChange>) {
		// The command validates every cell, including read-only inputs, before any mutation.
		val command = TruthTableCellsCommand(ref, changes)
		if (changes.any { ref.truthTable.getValue(it.row, it.column) != it.value }) {
			commandManager.execute(command)
		}
	}

	private inner class SetAction(private val bit: Bit) : AbstractAction(
		Translations.getString("antares.truthTable.fill", bit.toString()),
	) {
		override fun actionPerformed(e: ActionEvent?) {
			if (table.selectedRow < 0 || table.selectedColumn < 0) return
			val row = table.selectedRow
			val column = table.selectedColumn
			val singleCell = table.selectedRowCount == 1 && table.selectedColumnCount == 1
			fillSelection(bit)
			if (singleCell && table.convertColumnIndexToModel(column) >= ref.truthTable.inputColumnCount) {
				forwardSelection(row, column)
			}
		}

		private fun forwardSelection(row: Int, column: Int) {
			val outputColumns = (0 until table.columnCount).filter {
				table.convertColumnIndexToModel(it) >= ref.truthTable.inputColumnCount
			}
			if (outputColumns.isEmpty()) return
			val nextColumn = outputColumns.firstOrNull { it > column }
			val nextRow = if (nextColumn == null) (row + 1) % table.rowCount else row
			table.changeSelection(nextRow, nextColumn ?: outputColumns.first(), false, false)
			table.scrollRectToVisible(table.getCellRect(table.selectedRow, table.selectedColumn, true))
		}
	}
}
