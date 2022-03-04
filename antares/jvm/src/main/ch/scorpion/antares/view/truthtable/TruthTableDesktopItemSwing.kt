package ch.scorpion.antares.view.truthtable

import ch.scorpion.antares.model.expression.BooleanExpressionNotation
import ch.scorpion.antares.model.quinemccluskey.QmcToBooleanExpression
import ch.scorpion.antares.model.quinemccluskey.minimizeToDNF
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.truthtable.*
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.edit.CommandManager
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
import kotlin.math.max

class TruthTableDesktopItemSwing(
	item: TruthTableLibraryItem,
	private val commandManager: CommandManager,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphDesktopItemPanelSwing() {

	companion object {
		private const val CELL_FONT_SIZE = 18
		private const val COLUMN_WIDTH = 40

		private val FOREGROUND = UIManager.getColor("TextField.foreground")
		private val BACKGROUND = UIManager.getColor("TextField.background")
		private val INACTIVE_FOREGROUND = UIManager.getColor("TextField.inactiveForeground")
		private val INACTIVE_BACKGROUND = UIManager.getColor("TextField.inactiveBackground")
	}

	private val ref = TruthTableReference(item)

	private val headerPanel = GraphDesktopItemHeaderPanelSwing(
		this,
		JLabel("${Translations.getString("library.element.truthTable.name")} \"${item.truthTable.name.getTranslation()}\""),
		allowClose = true)

	private val closeViewRequestHandler: EventHandler<CloseViewRequest> = { handle(it) }

	private val tableModel = TruthTableTableModel(ref)

	private val table = JTable(tableModel)

	private val scrollPane = JScrollPane(table)

	private val cellFont = table.font.deriveFont(CELL_FONT_SIZE.toFloat())

	private val generateExpressionsAction = GenerateExpressionsAction()

	private val expressionsTextArea = JTextArea()

	init {
		eventBus.register(CloseViewRequest::class, closeViewRequestHandler)
		buildUI()

		defineSetActions()

		ref.addDataListener {
			expressionsTextArea.text = ""
		}
	}

	private fun buildUI() {
		border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
		layout = BorderLayout(10, 10)

		add(headerPanel, BorderLayout.NORTH)
		add(createContentsPanel(), BorderLayout.CENTER)
		add(createExpressionsPanel(), BorderLayout.SOUTH)
	}

	private fun createContentsPanel(): JComponent {
		table.font = cellFont
		table.rowHeight = CELL_FONT_SIZE + 8
		table.autoResizeMode = JTable.AUTO_RESIZE_OFF
		table.setShowGrid(true)
		table.cellSelectionEnabled = true
		table.rowMargin = 1
		table.columnModel.columnMargin = 1

		expressionsTextArea.font = cellFont

		scrollPane.horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED

		updateColumnModels()

		val panel = JPanel(BorderLayout())
		val tipLabel = JLabel(Translations.getString("library.element.truthTable.tip"))
		tipLabel.border = BorderFactory.createEmptyBorder(5, 2, 5, 0)
		panel.add(tipLabel, BorderLayout.NORTH)
		panel.add(scrollPane, BorderLayout.CENTER)

		return panel
	}

	private fun createExpressionsPanel(): JComponent {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

		expressionsTextArea.alignmentX = Component.LEFT_ALIGNMENT
		expressionsTextArea.isEditable = false
		expressionsTextArea.rows = max(4, ref.truthTable.outputColumnCount)

		val expressionScrollPane = JScrollPane(expressionsTextArea)
		expressionScrollPane.alignmentX = Component.LEFT_ALIGNMENT
		expressionScrollPane.horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED
		expressionScrollPane.verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED

		val expressionsButton = JButton(ActionWrapperSwing(generateExpressionsAction))
		expressionsButton.alignmentX = Component.LEFT_ALIGNMENT

		panel.add(expressionsButton)
		panel.add(Box.createVerticalStrut(5))
		panel.add(expressionScrollPane)

		return panel
	}

	override fun addContextColorBorder(color: Color) { }

	override fun removeContextColorBorder() { }

	override val drawingView: DrawingView<GraphView>? get() = null

	override fun disposeItem() {
		eventBus.unregister(closeViewRequestHandler)
		ref.dispose()
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
			if (index < ref.truthTable.inputColumnCount) {
				tableColumn.cellRenderer = InputCellRenderer()
			} else {
				tableColumn.cellRenderer = OutputCellRenderer()
				tableColumn.cellEditor = OutputCellEditor(createOutputEditor())
			}
			tableColumn.preferredWidth = COLUMN_WIDTH
		}
	}

	private fun applyZebra(label: JLabel, row: Int, output: Boolean) {
		label.foreground = if (output) FOREGROUND else INACTIVE_FOREGROUND
		if (row.and(4) == 0) {
			label.background = BACKGROUND
		} else {
			label.background = INACTIVE_BACKGROUND
		}
	}

	private fun generateExpressions() {
		val builder = StringBuilder()
		with (ref.truthTable) {
			for (col in inputColumnCount until inputColumnCount + outputColumnCount) {
				val dnf = minimizeToDNF(getMinTerms(col), getDontCares(col), inputColumnCount)
				builder.append(
					BooleanExpressionNotation.fromProperties().dnfWriter
						.write(this, QmcToBooleanExpression(ref.truthTable, dnf).build(), col)
				)
				builder.append("\n")
			}
		}
		expressionsTextArea.text = builder.toString()
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

	private inner class OutputCellEditor(textField: JTextField) : DefaultCellEditor(textField) {
		override fun isCellEditable(anEvent: EventObject?): Boolean = false
	}

	private inner class SetAction(private val bit: Bit) : AbstractAction() {

		override fun actionPerformed(e: ActionEvent?) {
			val row = table.selectedRow
			var column = table.selectedColumn

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

	private inner class GenerateExpressionsAction
		: ch.scorpion.jabbah.base.AbstractAction("antares.action.truthTable.expressions")
	{
		override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
			generateExpressions()
		}
	}
}