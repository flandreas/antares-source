package ch.scorpion.antares.model.testcase.result

import ch.scorpion.antares.model.testcase.MatchedValue
import ch.scorpion.antares.model.testcase.TestRunResult
import ch.scorpion.antares.model.testcase.TestVector.Type.*
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.RowHeaderTable
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.richtext.RichTextTableCellRenderer
import java.awt.*
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * Displays the data test vector results of a single [TestRunResult] as a table.
 */
class SingleTestRunDataResultPanelSwing(
	private val result: TestRunResult
) : JPanel() {

	companion object {

		private val DESCRIPTION_CELL_RENDERER = DescriptionCellRenderer()
		private val INPUT_CELL_RENDERER = InputCellRenderer()
		private val OUTPUT_CELL_RENDERER = OutputCellRenderer()
		private const val COLUMN_WIDTH = 100

		private val ICON_COLOR = UIManager.getColor("Label.foreground")
		private val ICON_STROKE: Stroke = BasicStroke(2.0f)

		private val COLUMN_HEADER_BORDER = UIManager.getBorder("TableHeader.cellBorder")
	}

	private val summaryLabel = JLabel()
	private val table = JTable(TableModel(result))
	private val scrollPane = JScrollPane(table)

	private val firstRowHeaderIcon = FirstRowHeaderIcon(table.rowHeight)
	private val lineRowHeaderIcon = LineRowHeaderIcon(table.rowHeight)
	private val lastRowHeaderIcon = LastRowHeaderIcon(table.rowHeight)

	private val richTextPortNames: List<RichTextDrawable> = result.names.map { RichTextDrawable.of(it, Graphics2DJvm.fromAwtFont(table.font)) }

	init {
		buildUI()
		updateSummaryLabel()
		updateColumnModels()
		updateRowHeaders()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(UIBasics.DIALOG_BORDER, UIBasics.DIALOG_BORDER, 0, UIBasics.DIALOG_BORDER)

		table.autoResizeMode = JTable.AUTO_RESIZE_OFF
		table.setShowGrid(true)
		table.rowMargin = 1
		table.columnModel.columnMargin = 1
		table.tableHeader.defaultRenderer = TableColumnRenderer()

		add(summaryLabel, BorderLayout.NORTH)
		add(scrollPane, BorderLayout.CENTER)
	}

	private fun updateSummaryLabel() {
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

	private fun updateColumnModels() {
		table.columnModel.columns.asSequence().forEachIndexed { index, tableColumn ->
			tableColumn.cellRenderer = if (index == 0) {
				DESCRIPTION_CELL_RENDERER
			} else if (result.isOutput[index - 1]) {
				OUTPUT_CELL_RENDERER
			} else {
				INPUT_CELL_RENDERER
			}
			tableColumn.preferredWidth = COLUMN_WIDTH
		}
	}

	private fun updateRowHeaders() {
		val rowHeaderTable = RowHeaderTable(table, 30, RowHeaderRenderer())
		scrollPane.setRowHeaderView(rowHeaderTable)
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.tableHeader)
	}

	private class TableModel(
		private val result: TestRunResult
	) : AbstractTableModel() {

		override fun getRowCount(): Int = result.collector.size

		override fun getColumnCount(): Int = result.names.size + 1

		override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
			if (columnIndex == 0) {
				result.collector.get(rowIndex).description
			} else {
				result.collector.get(rowIndex).getValue(columnIndex - 1)
			}

		override fun getColumnName(column: Int): String =
			if (column == 0) {
				Translations.getString("antares.testcase.result.description.name")
			} else {
				result.names[column - 1]
			}
	}

	private class InputCellRenderer : DefaultTableCellRenderer() {
		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
			label.horizontalAlignment = SwingConstants.CENTER
			return label
		}
	}

	private class DescriptionCellRenderer : DefaultTableCellRenderer() {
		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
			label.horizontalAlignment = SwingConstants.LEFT
			return label
		}
	}

	private class OutputCellRenderer : DefaultTableCellRenderer() {

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
			label.horizontalAlignment = SwingConstants.CENTER
			label.isOpaque = true

			label.text = value.toString()
			when ((value as Value).state) {
				Value.State.FAILED -> {
					if (!isSelected) {
						label.background = UiUtil.failureBackgroundColor
						label.foreground = UiUtil.failureTextColor
					}
					label.text = "E: ${(value as MatchedValue).expected} / A: $value"
				}
				Value.State.PASSED -> {
					if (!isSelected) {
						label.background = UiUtil.successBackgroundColor
						label.foreground = UiUtil.successTextColor
					}
					if ((value as MatchedValue).expected.type == Value.Type.DONT_CARE) {
						label.text = "E: ${value.expected} / A: $value"
					}
				}
				else -> {
					label.background = UIManager.getColor("Label.background")
					label.foreground = UIManager.getColor("Label.foreground")
				}
			}

			return label
		}
	}

	private inner class TableColumnRenderer : RichTextTableCellRenderer() {

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as RichTextTableCellRenderer

			renderer.border = COLUMN_HEADER_BORDER
			if (column == 0) {
				renderer.horizontalAlignment = JLabel.LEFT
				renderer.richText = null
			} else {
				renderer.horizontalAlignment = JLabel.CENTER
				renderer.richText = richTextPortNames[column - 1]
			}
			return renderer
		}
	}

	private abstract class AbstractRowHeaderIcon(val height: Int) : Icon {
		override fun getIconHeight(): Int = height
		override fun getIconWidth(): Int = 10
	}

	private class FirstRowHeaderIcon(height: Int) : AbstractRowHeaderIcon(height) {
		override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
			g.color = ICON_COLOR
			(g as Graphics2D).stroke = ICON_STROKE
			g.drawLine(x + iconWidth, y + iconHeight / 2, x + iconWidth / 2, y + iconHeight / 2)
			g.drawLine(x + iconWidth / 2, y + iconHeight / 2, x + iconWidth / 2, y + iconHeight)
		}
	}

	private class LineRowHeaderIcon(height: Int) : AbstractRowHeaderIcon(height) {
		override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
			g.color = ICON_COLOR
			(g as Graphics2D).stroke = ICON_STROKE
			g.drawLine(x + iconWidth / 2, y, x + iconWidth / 2, y + iconHeight)
		}
	}

	private class LastRowHeaderIcon(height: Int) : AbstractRowHeaderIcon(height) {
		override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
			g.color = ICON_COLOR
			(g as Graphics2D).stroke = ICON_STROKE
			g.drawLine(x + iconWidth, y + iconHeight / 2, x + iconWidth / 2, y + iconHeight / 2)
			g.drawLine(x + iconWidth / 2, y + iconHeight / 2, x + iconWidth / 2, y)
		}
	}

	private inner class RowHeaderRenderer : RowHeaderTable.RowHeaderRenderer() {

		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel

			label.horizontalAlignment = JLabel.RIGHT
			label.text = null
			label.icon = when (result.collector.get(row).type) {
				Top -> null
				RunFirst -> firstRowHeaderIcon
				RunLine -> lineRowHeaderIcon
				RunLast -> lastRowHeaderIcon
			}

			return label
		}
	}
}