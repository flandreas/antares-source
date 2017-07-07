package ch.scorpion.jabbah.draw.style

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JTable
import javax.swing.table.TableCellRenderer
import ch.scorpion.jabbah.draw.style.StyleType

class StyleTypeEditor(styleProvider: StyleProvider) : ComboBoxPropertyEditor() {

    constructor(): this(DrawStyleModule.styleProvider)

    init {
        setAvailableValues(styleProvider.getStyleTypes().toTypedArray())
        (editor as JComboBox<*>).renderer = StyleTypeRenderer()
    }
}

class StyleTypeRenderer : DefaultListCellRenderer(), TableCellRenderer {

    override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        setValue(value as StyleType)

        if (isSelected) {
            foreground = list.selectionForeground
            background = list.selectionBackground
        } else {
            foreground = list.foreground
            background = list.background
        }
        font = list.font
        return this
    }

    override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        setValue(value as StyleType)

        if (isSelected) {
            foreground = table.selectionForeground
            background = table.selectionBackground
        } else {
            foreground = table.foreground
            background = table.background
        }
        font = table.font
        return this
    }

    private fun setValue(styleType: StyleType?) {
        // TODO I18N
        if (styleType == null) {
            icon = null
            text = "Keine"
        } else {
            text = styleType.description
        }
    }
}