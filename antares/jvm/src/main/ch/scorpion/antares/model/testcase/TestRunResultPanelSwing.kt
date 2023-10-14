package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.graphics.PredefinedColorIdentity
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Themes
import java.awt.*
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * Displays the results of a [TestcaseRunner] as a table.
 */
class TestRunResultPanelSwing(
	result: TestRunResult,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val INPUT_CELL_RENDERER = InputCellRenderer()
		private val OUTPUT_CELL_RENDERER = OutputCellRenderer()
		private const val COLUMN_WIDTH = 30

		fun showAsDialog(
			result: TestRunResult,
			parent: Frame = Frame.getFrames()[0]
		) {
			DialogBuilder<TestRunResultPanelSwing>(parent)
				.content { dialog -> TestRunResultPanelSwing(result) { dialog.dispose()} }
				.title(Translations.getString("antares.testcase.results.title"))
				.defaultButton { it.closeButton }
				.minimumSize(Dimension(200, 200))
				.preferredSize(Dimension(400, 300))
				.show()
		}
	}

	private val table = JTable(TableModel(result))
	private val scrollPane = JScrollPane(table)
	private val closeAction = CloseAction()
	private val closeButton = JButton(ActionWrapperSwing(closeAction))

	init {
		buildUI()
		updateColumnModels(result)
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		add(scrollPane, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(closeButton)
		add(buttonPanel, BorderLayout.SOUTH)
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
			label.background = when ((value as Value).state) {
				Value.State.FAILED -> failedBackgroundColor
				Value.State.PASSED -> successBackgroundColor
				else -> UIManager.getColor("Label.background")
			}
			label.foreground = when ((value as Value).state) {
				Value.State.FAILED -> failedTextColor
				Value.State.PASSED -> successTextColor
				else -> UIManager.getColor("Label.foreground")
			}
			return label
		}
	}

	private inner class CloseAction : AbstractAction("base.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler()
		}
	}
}