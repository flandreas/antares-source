package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.ShowSidebarPaneContentRequest
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.execution.issue.*
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class IssuesViewSwing(
	private val controller: IssuesViewController
) : JPanel(), IssuesView {

	companion object {
		private const val SETTING_COLUMN_WIDTHS = "jabbah.execution.issuesPanel.columnWidth"
		private val WARNING_ICON = UiUtil.themedIcon("/img/warning-16.png")
		private val ERROR_ICON = UiUtil.themedIcon("/img/error-16.png")
		private val COLUMN_NAMES = arrayOf(
			Translations.getString("issue.property.name.name"),
			Translations.getString("issue.property.origin.name"),
			Translations.getString("issue.property.context.name"),
			Translations.getString("issue.property.description.name")
		)
	}

	private val issueCollector = controller.issueCollector
	private val table = JTable(IssueTableModel())

	init {
		controller.view = this
		buildUI()
	}

	override fun dispose() {
		storeColumnsWidths()
	}

	override fun notifyNewIssues() {
		(table.model as IssueTableModel).fireTableDataChanged()
		controller.eventBus.post(ShowSidebarPaneContentRequest(this))
	}

	private fun buildUI() {
		table.autoResizeMode = JTable.AUTO_RESIZE_OFF

		table.columnModel.getColumn(0).preferredWidth = BaseModule.settings.getInt("$SETTING_COLUMN_WIDTHS.name", 200)
		table.columnModel.getColumn(1).preferredWidth = BaseModule.settings.getInt("$SETTING_COLUMN_WIDTHS.origin", 200)
		table.columnModel.getColumn(2).preferredWidth = BaseModule.settings.getInt("$SETTING_COLUMN_WIDTHS.context", 200)
		table.columnModel.getColumn(3).preferredWidth = BaseModule.settings.getInt("$SETTING_COLUMN_WIDTHS.description", 200)


		table.columnModel.getColumn(0).cellRenderer = NameCellRenderer()

		layout = BorderLayout()
		val scrollPane = JScrollPane(table)
		add(scrollPane, BorderLayout.CENTER)
	}

	private fun storeColumnsWidths() {
		BaseModule.settings.set("$SETTING_COLUMN_WIDTHS.name", table.columnModel.getColumn(0).width)
		BaseModule.settings.set("$SETTING_COLUMN_WIDTHS.origin", table.columnModel.getColumn(1).width)
		BaseModule.settings.set("$SETTING_COLUMN_WIDTHS.context", table.columnModel.getColumn(2).width)
		BaseModule.settings.set("$SETTING_COLUMN_WIDTHS.description", table.columnModel.getColumn(3).width)
	}

	private inner class IssueTableModel : AbstractTableModel() {

		override fun getColumnName(column: Int): String = COLUMN_NAMES[column]

		override fun getRowCount(): Int = issueCollector.size

		override fun getColumnCount(): Int = COLUMN_NAMES.size

		override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
			val issue = issueCollector.getIssue(rowIndex)
			return when (columnIndex) {
				0 -> issue.name
				1 -> issue.origin
				2 -> issue.context ?: ""
				3 -> issue.description ?: ""
				else -> ""
			}
		}
	}

	/** Renders the severity of an [Issue] as an icon and its name as text in a single column.*/
	private inner class NameCellRenderer : DefaultTableCellRenderer() {

		private var currentRow: Int = 0

		/** Overridden in order to capture the current row index. */
		override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
			currentRow = row
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
		}

		override fun setValue(value: Any?) {
			val issue = issueCollector.getIssue(currentRow)
			text = issue.name
			icon = when (issue.severity) {
				IssueSeverity.Warning -> WARNING_ICON
				IssueSeverity.Error -> ERROR_ICON
			}
		}
	}
}