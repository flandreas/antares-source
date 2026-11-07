package io.antarescircuit.jabbah.graph.ui.portrenaming

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.draw.view.AbstractViewAction
import io.antarescircuit.jabbah.draw.view.FocusDrawablePlayer
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphPort
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*
import javax.swing.table.AbstractTableModel

class GraphPortRenamingAction(
    private val editor: Editor
) : AbstractViewAction("graph.portRenamingPanel.action") {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        GraphPortRenamingPanelSwing.showAsDialog(name, editor)
    }
}

/**
 * A panel for editing the names of all [GraphPort] of a [Graph].
 */
class GraphPortRenamingPanelSwing(
    private val controller: GraphPortRenamingController,
    private val closeHandler: (GraphPortRenamingPanelSwing) -> Unit
) : JPanel(), GraphPortRenamingPanel {

    companion object {

        private const val SETTING_ZOOM_TO_PORT = "graph.renamingPanel.zoomToPort"

        fun showAsDialog(
            title: String,
            editor: Editor,
            parent: Frame = Frame.getFrames()[0]
        ) {
            val controller = GraphPortRenamingController(editor)

            DialogBuilder<GraphPortRenamingPanelSwing>(parent)
                .title(title)
                .content { dialog -> GraphPortRenamingPanelSwing(controller) { dialog.dispose() } }
                .defaultButton { it.closeButton }
                .preferredSize(Dimension(600, 400))
                .show()

            controller.dispose()
        }
    }

    private val table = object : JTable(GraphPortViewItemsTableModel()) {
        override fun changeSelection(rowIndex: Int, columnIndex: Int, toggle: Boolean, extend: Boolean) {
            super.changeSelection(rowIndex, columnIndex, toggle, extend)
            if (columnIndex == 0) {
                editCellAt(rowIndex, columnIndex)
                editor.textComponent.requestFocus()
            }
        }
    }

    private val editor = GraphPortRenamingEditor(controller)

    private val closeAction = CloseAction()

    private val closeButton = JButton(closeAction)

    private val errorLabel = JLabel("")

    private val zoomToPortCheckbox = JCheckBox(
        Translations.getString("graph.portRenamingPanel.zoomToPort"),
        BaseModule.settings.getBoolean(SETTING_ZOOM_TO_PORT, true))

    private val editable: Boolean get() = controller.editor.view.editable

    init {
        controller.view = this
        buildUI()
    }

    override fun dispose() {
        BaseModule.settings.set(SETTING_ZOOM_TO_PORT, zoomToPortCheckbox.isSelected)
    }

    override fun setErrorText(text: String) {
        errorLabel.text = if (StringUtils.isBlank(text)) {
            " "
        } else {
            text
        }
    }

    private fun buildUI() {
        layout = BorderLayout(0, 15)
        border = UIBasics.createDialogBorder()
        add(buildHeaderComponent(), BorderLayout.NORTH)
        add(buildContentPanel(), BorderLayout.CENTER)
        add(buildButtonPanel(), BorderLayout.SOUTH)
    }

    private fun buildHeaderComponent(): JComponent = zoomToPortCheckbox

    private fun buildContentPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 5))

        table.tableHeader.reorderingAllowed = false
        table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                selectionChanged()
            }
        }
        with (table.columnModel.getColumn(0)) {
            cellEditor = editor
            preferredWidth = 150
        }

        errorLabel.foreground = UiUtil.errorTextColor

        val scrollPane = JScrollPane(table)
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

        panel.add(scrollPane, BorderLayout.CENTER)
        panel.add(errorLabel, BorderLayout.SOUTH)

        return panel
    }

    private fun buildButtonPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        panel.add(Box.createHorizontalGlue())
        panel.add(closeButton)
        return panel
    }

    private fun selectionChanged() {
        if (table.selectedRowCount > 0) {
            val itemView = controller.items[table.selectedRow]
            controller.graphView.getWithId(itemView.id)?.let {
                controller.editor.view.selectionManager.deselectAll()
                controller.editor.view.selectionManager.select(it)
                if (zoomToPortCheckbox.isSelected) {
                    FocusDrawablePlayer.playFocus(it, controller.editor.view)
                }
            }
        }
    }

    private inner class GraphPortViewItemsTableModel : AbstractTableModel() {

        override fun getRowCount(): Int = controller.items.size

        override fun getColumnCount(): Int = 2

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
            when (columnIndex) {
                0 -> controller.items[rowIndex].name
                1 -> controller.items[rowIndex].type
                else -> throw IllegalArgumentException("invalid column index: $columnIndex")
            }

        override fun getColumnName(column: Int): String =
            when (column) {
                0 -> Translations.getString("graph.portRenamingPanel.column.name")
                1 -> Translations.getString("graph.portRenamingPanel.column.type")
                else -> throw IllegalArgumentException("invalid column index: $column")
            }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = editable && columnIndex == 0
    }

    private inner class CloseAction : AbstractAction(Translations.getString("base.action.close.name")) {
        override fun actionPerformed(e: java.awt.event.ActionEvent?) {
            closeHandler(this@GraphPortRenamingPanelSwing)
        }
    }
}