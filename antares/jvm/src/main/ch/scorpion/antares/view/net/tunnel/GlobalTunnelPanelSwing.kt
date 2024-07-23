package ch.scorpion.antares.view.net.tunnel

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.DataFormPanel
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.PlaceholderTextField
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.richtext.RichTextTableCellRenderer
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.AbstractTableModel

class GlobalTunnelAction : AbstractAction("antares.globalTunnels.action") {
    override fun execute(event: ActionEvent) {
        InvocationHandler.invoke {
            GlobalTunnelPanelSwing.showAsDialog()
        }
    }
}

class GlobalTunnelPanelSwing(
    private val controller: GlobalTunnelPanelController,
    private val closeHandler: () -> Unit
): JPanel(), GlobalTunnelPanel {

    companion object {
        fun showAsDialog(
            parent: Frame = Frame.getFrames()[0]
        ) {
            val controller = GlobalTunnelPanelController()

            DialogBuilder<GlobalTunnelPanelSwing>(parent)
                .title(Translations.getString("antares.globalTunnels.title"))
                .content { dialog ->
                    GlobalTunnelPanelSwing(controller) {
                        dialog.dispose()
                    }.also { it.controller.load() }
                }
                .defaultButton { it.closeButton }
                .preferredSize(Dimension(800, 600))
                .minimumSize(Dimension(200, 300))
                .show()
        }
    }

    private val closeAction = CloseAction()
    private val closeButton = JButton(ActionWrapperSwing(closeAction))

    private val tunnelNamesTable = buildTunnelNamesTable()
    private val usagesTable = buildUsagesTable()

    private val tunnelNameSearchField = PlaceholderTextField(
        placeholder = Translations.getString("base.action.search.name"),
        columns = 20,
        showClearButton = true)

    init {
        controller.view = this
        buildUI()
    }

    /** ---- [GlobalTunnelPanel] interface */

    override fun dispose() {}

    override fun updateResult() {
        (tunnelNamesTable.model as AbstractTableModel).fireTableDataChanged()
        usagesTable.model = UsageTableModel(emptyList())
    }

    /** ---- [GlobalTunnelPanelSwing] */

    private fun buildUI() {
        layout = BorderLayout(10, 20)
        border = UIBasics.createDialogBorder()
        add(buildContent(), BorderLayout.CENTER)
        add(buildButtonPanel(), BorderLayout.SOUTH)

        tunnelNamesTable.selectionModel.addListSelectionListener {
            updateUsagesTable()
        }

        tunnelNameSearchField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) { search() }
            override fun removeUpdate(e: DocumentEvent?) { search() }
            override fun changedUpdate(e: DocumentEvent?) { search() }
            private fun search() {
                controller.filterTunnelNames(tunnelNameSearchField.text)
            }
        })
    }

    private fun buildContent(): JPanel {
        val panel = JPanel(BorderLayout(0, 10))

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        splitPane.dividerLocation = 150
        splitPane.add(buildTunnelNamesPanel())
        splitPane.add(buildUsagesPanel())

        panel.add(buildFilterPanel(), BorderLayout.NORTH)
        panel.add(splitPane, BorderLayout.CENTER)
        return panel
    }

    private fun buildFilterPanel(): JPanel {
        val form = DataFormPanel()
        form.addLabeledRow(Translations.getString("antares.globalTunnels.tunnelName.name"), tunnelNameSearchField)
        return form
    }

    private fun buildTunnelNamesPanel(): JComponent {
        val scrollPane = JScrollPane(tunnelNamesTable)
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        return scrollPane
    }

    private fun buildUsagesPanel(): JComponent {
        val usagesScrollPane = JScrollPane(usagesTable)
        usagesScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        usagesScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        return usagesScrollPane
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

        override fun getRowCount(): Int = controller.filteredTunnelNames.size

        override fun getColumnCount(): Int = 1

        override fun getColumnName(column: Int): String =
            when (column) {
                0 -> Translations.getString("antares.globalTunnels.tunnelName.name")
                else -> throw IllegalArgumentException("Illegal column $column")
            }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
            when (columnIndex) {
                0 -> controller.filteredTunnelNames[rowIndex]
                else -> throw IllegalArgumentException("Illegal column $columnIndex")
            }
    }

    private inner class TunnelNamesRenderer : RichTextTableCellRenderer(false) {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            val renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as RichTextTableCellRenderer
            renderer.horizontalAlignment = JLabel.LEFT
            renderer.richText = controller.allRichTextTunnelNames[value as String]
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
            val name = tunnelNamesTable.model.getValueAt(tunnelNamesTable.selectedRow, 0) as String
            usagesTable.model = UsageTableModel(controller.getUsages(name))
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