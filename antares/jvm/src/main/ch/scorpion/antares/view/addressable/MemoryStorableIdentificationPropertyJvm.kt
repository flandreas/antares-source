package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.MemoryStorableIdentification
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.Component
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer

class MemoryStorableIdentificationRenderer : DefaultTableCellRenderer(), ListCellRenderer<MemoryStorableIdentification> {

    override fun getListCellRendererComponent(
        list: JList<out MemoryStorableIdentification>,
        value: MemoryStorableIdentification?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        text = value?.toString() ?: Translations.getString("element.property.memoryStorable.none.name")

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

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        text = value?.toString() ?: Translations.getString("element.property.memoryStorable.none.name")

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
}

class MemoryStorableIdentificationEditor : AbstractPropertyEditor() {

    private val comboBoxEditor = ComboBoxPropertyEditor()
    private val comboBox: JComboBox<MemoryStorableIdentification> get() = comboBoxEditor.customEditor as JComboBox<MemoryStorableIdentification>

    init {
        comboBox.renderer = MemoryStorableIdentificationRenderer()
        comboBox.model = DefaultComboBoxModel(MemoryStorableIdentification.getAll().toTypedArray())
            .also { it.insertElementAt(null, 0) }

        comboBox.isEditable = false
        editor = comboBox
    }

    override fun getValue(): Any? {
        val value = comboBox.editor.item
        return if (value is String && StringUtils.isBlank(value)) {
            null
        } else {
            value
        }
    }

    override fun setValue(value: Any?) {
        comboBoxEditor.value = value
    }
}