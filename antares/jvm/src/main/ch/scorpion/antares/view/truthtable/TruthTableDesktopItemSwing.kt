package ch.scorpion.antares.view.truthtable

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.FocusJTable
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.ui.desktop.AbstractGraphDesktopItemPanelSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopItemHeaderPanelSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemCloseRequest
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.*
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
import javax.swing.table.DefaultTableCellRenderer

class TruthTableDesktopItemSwing(
	private val truthTable: TruthTable,
	viewManager: ViewManager,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphDesktopItemPanelSwing() {

	companion object {
		private const val CELL_FONT_SIZE = 25
		private const val COLUMN_WIDTH = 80
	}

	private val headerPanel = GraphDesktopItemHeaderPanelSwing(this, JLabel("TODO: Truth Table"), allowClose = true)

	private val closeViewRequestHandler: EventHandler<CloseViewRequest> = { handle(it) }

	private val table = FocusJTable(TruthTableTableModel(truthTable))

	private val scrollPane = JScrollPane(table)

	private val cellFont = table.font.deriveFont(CELL_FONT_SIZE.toFloat())

	init {
		eventBus.register(CloseViewRequest::class, closeViewRequestHandler)
		buildUI()

		defineSetActions()
	}

	private fun buildUI() {
		table.font = cellFont
		table.rowHeight = CELL_FONT_SIZE + 5
		table.autoResizeMode = JTable.AUTO_RESIZE_OFF
		table.setShowGrid(true)
		table.cellSelectionEnabled = true

		scrollPane.horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED

		updateColumnModels()

		layout = BorderLayout()
		add(headerPanel, BorderLayout.NORTH)
		add(scrollPane, BorderLayout.CENTER)
	}

	override fun addContextColorBorder(color: Color) { }

	override fun removeContextColorBorder() { }

	override val drawingView: DrawingView<GraphView>? get() = null

	override fun disposeItem() {
		eventBus.unregister(closeViewRequestHandler)
	}

	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

	override fun createCloseRequest(): Any = CloseViewRequest(this)

	/** ---- [TruthTableDesktopItemSwing] */

	private fun handle(request: CloseViewRequest) {
		if (request.view === this) {
			eventBus.postTwoPhase(
				prepareEvent = GraphDesktopViewItemCloseRequest(this, isRoot = true),
				execEvent = GraphDesktopViewItemCloseRequest(this, isRoot = true)
			)
		}
	}

	private fun defineSetActions() {
		table.inputMap.put(KeyStroke.getKeyStroke("0"), "Action0")
		table.actionMap.put("Action0", SetAction(Bit.False))
		table.inputMap.put(KeyStroke.getKeyStroke("1"), "Action1")
		table.actionMap.put("Action1", SetAction(Bit.True))
		table.inputMap.put(KeyStroke.getKeyStroke("X"), "ActionX")
		table.actionMap.put("ActionX", SetAction(Bit.Error))
	}

	private fun createOutputEditor(): JTextField {
		val textField = JTextField()
		textField.font = cellFont
		textField.isEditable = false
		return textField
	}

	private fun updateColumnModels() {
		table.columnModel.columns.asSequence().forEachIndexed { index, tableColumn ->
			if (index < truthTable.inputColumnCount) {
				tableColumn.cellRenderer = InputCellRenderer()
			} else {
				tableColumn.cellRenderer = OutputCellRenderer()
				tableColumn.cellEditor = OutputCellEditor(createOutputEditor())
			}
			tableColumn.preferredWidth = COLUMN_WIDTH
		}
	}

	private inner class InputCellRenderer : DefaultTableCellRenderer() {

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
			renderer.horizontalAlignment = SwingConstants.CENTER
			renderer.font = cellFont
			renderer.foreground = UIManager.getColor("TextField.inactiveForeground")
			return renderer
		}
	}

	private inner class OutputCellRenderer : DefaultTableCellRenderer() {

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
			renderer.horizontalAlignment = SwingConstants.CENTER
			renderer.font = cellFont
			return renderer
		}
	}

	private inner class OutputCellEditor(val textField: JTextField) : DefaultCellEditor(textField) {
		override fun isCellEditable(anEvent: EventObject?): Boolean = false
	}

	private inner class SetAction(private val bit: Bit) : AbstractAction() {

		override fun actionPerformed(e: ActionEvent?) {
			val row = table.selectedRow
			var column = table.selectedColumn

			if (column in truthTable.inputColumnCount until table.columnCount) {
				// TODO: Issue Command
				truthTable.setValue(row, column, bit)
			}

			forwardSelection(row, column)
		}

		private fun forwardSelection(row: Int, column: Int) {
			var r = row
			var c = column

			c++
			if (c >= table.columnCount) {
				c = truthTable.inputColumnCount
				r++
				if (r >= truthTable.rowsCount) {
					r = 0
				}
			}
			table.changeSelection(r, c, toggle = false, extend = false)
		}
	}
}