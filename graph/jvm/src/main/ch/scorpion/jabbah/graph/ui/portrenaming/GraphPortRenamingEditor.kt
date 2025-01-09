package ch.scorpion.jabbah.graph.ui.portrenaming

import ch.scorpion.jabbah.base.logger
import java.awt.Component
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.DefaultCellEditor
import javax.swing.JTable
import javax.swing.JTextField

class GraphPortRenamingEditor(
    private val controller: GraphPortRenamingController
) : DefaultCellEditor(JTextField()) {

    companion object {
        private val LOG by logger(GraphPortRenamingEditor::class)
    }

    private var oldValue: String = ""
    private var table: JTable? = null
    private var row: Int = 0
    private var col: Int = 0

    val textComponent: JTextField get() = component as JTextField

    init {
        clickCountToStart = 0

        component.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                with (textComponent) {
                    selectAll()
                    oldValue = retrieveCurrentValue()
                }
            }

            override fun focusLost(e: FocusEvent?) {
                handleFocusLost()
            }
        })
    }

    override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
        this.row = row
        this.col = column
        this.table = table
        return super.getTableCellEditorComponent(table, value, isSelected, row, column)
    }

    private fun retrieveCurrentValue(): String = controller.items[row].name

    private fun handleFocusLost() {
        val newValue = textComponent.text
        if (newValue != oldValue) {
            LOG.trace("Changed $row,$col from $oldValue to $newValue")
            try {
                controller.updateName(row, newValue)
            } catch (e: Throwable) {
                LOG.debug("Error in updateName: ${e.message}")
            }
        }
    }
}