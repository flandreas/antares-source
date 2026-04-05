package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.edit.model.image.ImageRepository
import io.antarescircuit.jabbah.edit.module.EditModule
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.Component
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer

class ImageIdentificationRenderer : DefaultTableCellRenderer(), ListCellRenderer<ImageIdentification> {
    override fun getListCellRendererComponent(
        list: JList<out ImageIdentification>,
        value: ImageIdentification?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        text = value?.toString() ?: Translations.getString("edit.property.image.none.name")

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
        text = value?.toString() ?: Translations.getString("edit.property.image.none.name")

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

/**
 * Offers all [ImageIdentification]s of the current [ImageRepository] for selection.
 */
class ImageIdentificationEditor : AbstractPropertyEditor() {

    private val comboBoxEditor = ComboBoxPropertyEditor()
    private val comboBox: JComboBox<ImageIdentification> get() = comboBoxEditor.customEditor as JComboBox<ImageIdentification>

    init {
        comboBox.renderer = ImageIdentificationRenderer()
        comboBox.model = DefaultComboBoxModel(EditModule.imageRepository.getAllImageIds().toTypedArray())
            .also { it.insertElementAt(null, 0) }

        comboBox.isEditable = true
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