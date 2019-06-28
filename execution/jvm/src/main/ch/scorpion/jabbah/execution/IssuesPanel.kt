package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.issue.Issue
import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.execution.issue.IssueCollectorEvent
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.execution.module.ExecutionModule
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/** Displays the current [Issue]s of an [IssueCollector] as a table.*/
class IssuesPanel(
	private val issueCollector: IssueCollector = ExecutionModule.issueCollector,
	private val eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

	companion object {
		private const val SETTING_COLUMN_WIDTHS = "jabbah.execution.issuesPanel.columnWidth"
		private val WARNING_ICON = ImageIcon(IssuesPanel::class.java.getResource("/img/warning-16.png"))
		private val ERROR_ICON = ImageIcon(IssuesPanel::class.java.getResource("/img/error-16.png"))
		private val COLUMN_NAMES = arrayOf(
			Translations.getString("issue.property.name.name"),
			Translations.getString("issue.property.origin.name"),
			Translations.getString("issue.property.context.name"),
			Translations.getString("issue.property.description.name")
		)
	}

	private val table = JTable(IssueTableModel())

	private val issueCollectionEventHandler: EventHandler<IssueCollectorEvent> = {
		(table.model as IssueTableModel).fireTableDataChanged()
	}

	init {
		eventBus.register(IssueCollectorEvent::class, issueCollectionEventHandler)
		buildUI()
	}

	fun dispose() {
		eventBus.unregister(IssueCollectorEvent::class, issueCollectionEventHandler)
		storeColumnsWidths()
	}

	/** Clears all [Issue]s.*/
	fun clear() {
		issueCollector.clear()
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

class ClearIssuesPanelAction(private val issuesPanel: IssuesPanel) : AbstractAction("graph.action.clearIssuesPanel") {

	init {
		imagePath = "/img/trash-16.png"
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		issuesPanel.clear()
	}
}