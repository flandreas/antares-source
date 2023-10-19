package ch.scorpion.antares.model.testcase.result

import ch.scorpion.antares.model.testcase.MatchedValue
import ch.scorpion.antares.model.testcase.TestRunResult
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.graphics.PredefinedColorIdentity
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import ch.scorpion.jabbah.draw.style.Themes
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * Displays a single [TestRunResult] as a table.
 */
class SingleTestRunResultPanelSwing(
	result: TestRunResult
) : JPanel() {

	companion object {

		private val INPUT_CELL_RENDERER = InputCellRenderer()
		private val OUTPUT_CELL_RENDERER = OutputCellRenderer()
		private const val COLUMN_WIDTH = 100
	}

	private val summaryLabel = JLabel()
	private val table = JTable(TableModel(result))
	private val scrollPane = JScrollPane(table)

	init {
		buildUI()
		updateSummaryLabel(result)
		updateColumnModels(result)
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		table.autoResizeMode = JTable.AUTO_RESIZE_OFF
		table.setShowGrid(true)
		table.rowMargin = 1
		table.columnModel.columnMargin = 1

		add(summaryLabel, BorderLayout.NORTH)
		add(scrollPane, BorderLayout.CENTER)
	}

	private fun updateSummaryLabel(result: TestRunResult) {
		if (result.errorMessage != null) {
			summaryLabel.text = result.errorMessage
		} else {
			val failedCount = result.failedCount
			if (failedCount == 0) {
				summaryLabel.text = Translations.getString("antares.testcase.results.summary.passed")
			} else {
				summaryLabel.text = Translations.getString("antares.testcase.results.summary.failed", failedCount)
			}
		}
	}

	private fun updateColumnModels(result: TestRunResult) {
		table.columnModel.columns.asSequence().forEachIndexed { index, tableColumn ->
			tableColumn.cellRenderer = if (result.isOutput[index]) {
				OUTPUT_CELL_RENDERER
			} else {
				INPUT_CELL_RENDERER
			}
			tableColumn.preferredWidth = COLUMN_WIDTH
		}
	}

	private class TableModel(
		private val result: TestRunResult
	) : AbstractTableModel() {

		override fun getRowCount(): Int = result.collector.size

		override fun getColumnCount(): Int = result.names.size

		override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
			result.collector.get(rowIndex).getValue(columnIndex)

		override fun getColumnName(column: Int): String = result.names[column]
	}

	private class InputCellRenderer : DefaultTableCellRenderer() {
		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
			label.horizontalAlignment = SwingConstants.CENTER
			return label
		}
	}

	private class OutputCellRenderer : DefaultTableCellRenderer() {

		private val failedColor = Themes.get<AntaresTheme>().error
		private val failedBackgroundColor = Graphics2DJvm.toAwtColor(failedColor.backgroundColor)
		private val failedTextColor = Graphics2DJvm.toAwtColor(failedColor.textColor)

		private val successColor = PredefinedColorRepository.withIdentity(PredefinedColorIdentity.Green)!!
		private val successBackgroundColor = Graphics2DJvm.toAwtColor(successColor.color.backgroundColor)
		private val successTextColor = Graphics2DJvm.toAwtColor(successColor.color.textColor)

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
			label.horizontalAlignment = SwingConstants.CENTER
			label.isOpaque = true

			label.text = value.toString()
			when ((value as Value).state) {
				Value.State.FAILED -> {
					label.background = failedBackgroundColor
					label.foreground = failedTextColor
					label.text = "E: ${(value as MatchedValue).expected} / A: $value"
				}
				Value.State.PASSED -> {
					label.background = successBackgroundColor
					label.foreground = successTextColor
				}
				else -> {
					label.background = UIManager.getColor("Label.background")
					label.foreground = UIManager.getColor("Label.foreground")
				}
			}

			return label
		}
	}
}