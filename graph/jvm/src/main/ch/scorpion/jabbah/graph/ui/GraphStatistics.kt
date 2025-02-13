package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.richtext.RichTextTableCellRenderer
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.element.GraphElementCollector
import ch.scorpion.jabbah.graph.model.element.GraphElementCollectorResult
import ch.scorpion.jabbah.graph.model.element.GraphElementCollectorResultEntry
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.*
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/** Provides in-depth statistical information about the currently open [Graph].*/
class GraphStatisticsAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractViewAction("graph.statistics.action", viewManager = viewManager) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		val graphView = (viewManager.activeView!!.view as DrawingView<*>).drawing as GraphView
		GraphStatisticsPanel.showAsDialog(title = name, graph = graphView.graph!!)
	}
}

/** Displays the result of a [GraphElementCollector] run for the specified [Graph].*/
class GraphStatisticsPanel(graph: Graph) : JPanel() {

	companion object {

		private const val PREF_DIALOG_WIDTH = 400
		private const val PREF_DIALOG_HEIGHT = 500
		private const val PREF_COUNT_COL_WIDTH = 70

		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			title: String,
			graph: Graph
		) {
			JOptionPane.showConfirmDialog(
				parent,
				GraphStatisticsPanel(graph),
				title,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE
			)
		}
	}

	private val infoText = JTextArea(Translations.getString("graph.statistics.description"))

	private val countRenderer = DefaultTableCellRenderer().apply {
		horizontalAlignment = JLabel.RIGHT
	}

	private val basicFont = UIManager.getFont("Tree.font")
	private val subGraphFont = UIManager.getFont("Tree.font").deriveFont(Font.ITALIC)

	private val elementRenderer = object : RichTextTableCellRenderer() {
		override fun getTableCellRendererComponent(
			table: JTable?,
			value: Any?,
			isSelected: Boolean,
			hasFocus: Boolean,
			row: Int,
			column: Int
		): Component {
			val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as RichTextTableCellRenderer
			label.richText = (value as GraphElementCollectorResultEntry).richText
			label.font = if (value.isScripted) {
				subGraphFont
			} else {
				basicFont
			}
			label.icon = if (value.clazz == SubGraphVerticeRef::class) {
				value.graphType?.let { MetaGraphIconProvider.provideIcon(it, current = false, value.isScripted) }
			} else {
				null
			}
			return label
		}
	}

	init {
		buildUI()
		InvocationHandler.invoke {
			displayStatistics(GraphElementCollector(font = Graphics2DJvm.fromAwtFont(font)).collect(graph))
		}
	}

	private fun buildUI() {
		layout = BorderLayout()
		preferredSize = Dimension(PREF_DIALOG_WIDTH, PREF_DIALOG_HEIGHT)
		add(JLabel(Translations.getString("graph.statistics.calculating")), BorderLayout.NORTH)
	}

	private fun displayStatistics(result: GraphElementCollectorResult) {
		removeAll()

		infoText.isEditable = false
		infoText.lineWrap = true
		infoText.wrapStyleWord = true

		add(infoText, BorderLayout.NORTH)

		val tabPane = JTabbedPane()
		tabPane.add(Translations.getString("graph.statistics.immediate.name"), buildPage(buildTable(result.immediate.sortedDescending())))
		tabPane.add(Translations.getString("graph.statistics.flat.name"), buildPage(buildTable(result.flat.sortedDescending())))
		tabPane.add(Translations.getString("graph.statistics.deep.name"), buildPage(buildTable(result.deep.sortedDescending())))
		add(tabPane, BorderLayout.CENTER)

		tabPane.setToolTipTextAt(0, Translations.getString("graph.statistics.immediate.desc"))
		tabPane.setToolTipTextAt(1, Translations.getString("graph.statistics.flat.desc"))
		tabPane.setToolTipTextAt(2, Translations.getString("graph.statistics.deep.desc"))

		revalidate()
		repaint()
	}

	private fun buildTable(entries: List<GraphElementCollectorResultEntry>): JTable {
		val table = JTable(StatisticsTableModel(entries))
		table.autoResizeMode = JTable.AUTO_RESIZE_OFF
		table.isEnabled = false
		table.rowHeight = 24
		table.columnModel.getColumn(0).apply {
			preferredWidth = PREF_COUNT_COL_WIDTH
			cellRenderer = countRenderer
		}
		table.columnModel.getColumn(1).apply {
			preferredWidth = PREF_DIALOG_WIDTH - PREF_COUNT_COL_WIDTH - 2
			cellRenderer = elementRenderer
		}
		return table
	}

	private fun buildPage(content: JComponent): JComponent {
		val scrollPane = JScrollPane(content)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		return scrollPane
	}

	private class StatisticsTableModel(
		private val entries: List<GraphElementCollectorResultEntry>
	) : AbstractTableModel() {

		override fun getRowCount(): Int = entries.size

		override fun getColumnCount(): Int = 2

		override fun getColumnName(column: Int): String =
			when (column) {
				0 -> Translations.getString("graph.statistics.column.count.name")
				1 -> Translations.getString("graph.statistics.column.element.name")
				else -> throw IllegalArgumentException("Illegal column $column")
			}

		override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
			when (columnIndex) {
				0 -> entries[rowIndex].count
				1 -> entries[rowIndex]
				else -> throw IllegalArgumentException("Illegal column $columnIndex")
			}
	}
}