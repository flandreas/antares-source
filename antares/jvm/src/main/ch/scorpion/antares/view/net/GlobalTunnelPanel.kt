package ch.scorpion.antares.view.net

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.richtext.RichTextTableCellRenderer
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*
import javax.swing.table.AbstractTableModel

class GlobalTunnelAction : AbstractAction("antares.globalTunnels.action") {
    override fun execute(event: ActionEvent) {
        InvocationHandler.invoke {
            GlobalTunnelPanel.showAsDialog(GlobalTunnelCollector().collect())
        }
    }
}

class GlobalTunnelPanel(
    private val result: GlobalTunnelCollectionResult,
    private val closeHandler: () -> Unit
): JPanel() {

    companion object {
        fun showAsDialog(
            result: GlobalTunnelCollectionResult,
            parent: Frame = Frame.getFrames()[0]
        ) {
            DialogBuilder<GlobalTunnelPanel>(parent)
                .title(Translations.getString("antares.globalTunnels.title"))
                .content { dialog -> GlobalTunnelPanel(result) {
                    dialog.dispose()
                } }
                .defaultButton { it.closeButton }
                .preferredSize(Dimension(800, 600))
                .minimumSize(Dimension(200, 300))
                .show()
        }
    }

    private val closeAction = CloseAction()
    private val closeButton = JButton(ActionWrapperSwing(closeAction))

    private var tunnelNames: List<String>
    private var richTextTunnelNames: List<RichTextDrawable>

    private val tunnelNamesTable = buildTunnelNamesTable()
    private val usagesTable = buildUsagesTable()


    init {
        val font = Graphics2DJvm.fromAwtFont(tunnelNamesTable.font)
        tunnelNames = result.keys.sorted()
        richTextTunnelNames = tunnelNames.map { RichTextDrawable.of(it, font) }
        buildUI()
    }

    private fun buildUI() {
        layout = BorderLayout(10, 20)
        border = UIBasics.createDialogBorder()
        add(buildContent(), BorderLayout.CENTER)
        add(buildButtonPanel(), BorderLayout.SOUTH)

        tunnelNamesTable.selectionModel.addListSelectionListener {
            updateUsagesTable()
        }
    }

    private fun buildContent(): JPanel {
        val panel = JPanel(BorderLayout())

        val tunnelNamesScrollPane = JScrollPane(tunnelNamesTable)
        tunnelNamesScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        tunnelNamesScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

        val usagesScrollPane = JScrollPane(usagesTable)
        usagesScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        usagesScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        splitPane.dividerLocation = 150
        splitPane.add(tunnelNamesScrollPane)
        splitPane.add(usagesScrollPane)

        panel.add(splitPane, BorderLayout.CENTER)
        return panel
    }

    private fun buildButtonPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        panel.add(Box.createHorizontalGlue())
        panel.add(closeButton)
        return panel
    }

    private fun buildTunnelNamesTable(): JTable {
        val table = JTable(TunnelNamesTableModel())
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.columnModel.getColumn(0).cellRenderer = TunnelNamesRenderer()
        return table
    }

    private inner class TunnelNamesTableModel : AbstractTableModel() {

        override fun getRowCount(): Int = tunnelNames.size

        override fun getColumnCount(): Int = 1

        override fun getColumnName(column: Int): String =
            when (column) {
                0 -> Translations.getString("antares.globalTunnels.tunnelName.name")
                else -> throw IllegalArgumentException("Illegal column $column")
            }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
            when (columnIndex) {
                0 -> tunnelNames[rowIndex]
                else -> throw IllegalArgumentException("Illegal column $columnIndex")
            }
    }

    private inner class TunnelNamesRenderer : RichTextTableCellRenderer(false) {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            val renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as RichTextTableCellRenderer
            renderer.horizontalAlignment = JLabel.LEFT
            renderer.richText = richTextTunnelNames[row]
            return renderer
        }
    }

    private fun buildUsagesTable(): JTable {
        val table = JTable(UsageTableModel(emptyList()))
        table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        return table
    }

    private class UsageTableModel(
        private val usages: List<GlobalTunnelUsage>
    ): AbstractTableModel() {


        override fun getRowCount(): Int = usages.size

        override fun getColumnCount(): Int = 4

        override fun getColumnName(column: Int): String =
            when (column) {
                0 -> Translations.getString("antares.globalTunnels.flowDirection.out.name")
                1 -> Translations.getString("antares.globalTunnels.flowDirection.in.name")
                2 -> Translations.getString("antares.globalTunnels.flowDirection.inOut.name")
                3 -> Translations.getString("antares.globalTunnels.circuitName.name")
                else -> throw IllegalArgumentException("Illegal column $column")
            }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
            when (columnIndex) {
                0 -> usages[rowIndex].outFlowDirection
                1 -> usages[rowIndex].inFlowDirection
                2 -> usages[rowIndex].inOutFlowDirection
                3 -> usages[rowIndex].circuitName
                else -> throw IllegalArgumentException("Illegal column $columnIndex")
            }

        override fun getColumnClass(columnIndex: Int): Class<*> =
            when (columnIndex) {
                0, 1, 2 -> java.lang.Boolean::class.java
                3 -> java.lang.String::class.java
                else -> throw IllegalArgumentException("Illegal column $columnIndex")
        }
    }

    private fun updateUsagesTable() {
        if (tunnelNamesTable.selectedRow >= 0) {
            val name =tunnelNamesTable.model.getValueAt(tunnelNamesTable.selectedRow, 0)
            usagesTable.model = UsageTableModel(result[name]!!)
        } else {
            usagesTable.model = UsageTableModel(emptyList())
        }
        usagesTable.columnModel.getColumn(0).preferredWidth = 50
        usagesTable.columnModel.getColumn(1).preferredWidth = 50
        usagesTable.columnModel.getColumn(2).preferredWidth = 50
        usagesTable.columnModel.getColumn(3).preferredWidth = 300
    }

    private inner class CloseAction : AbstractAction("base.action.close") {
        override fun execute(event: ActionEvent) {
            closeHandler()
        }
    }
}