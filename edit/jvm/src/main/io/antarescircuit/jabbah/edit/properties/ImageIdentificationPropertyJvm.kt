package io.antarescircuit.jabbah.edit.properties

import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.swing.ToStringRenderer
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.edit.model.image.ImageRepository
import io.antarescircuit.jabbah.edit.module.EditModule
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class ImageIdentificationRenderer : ToStringRenderer<ImageIdentification>(
    Translations.getString("edit.property.image.none.name")
)

/**
 * Offers all [ImageIdentification]s of the current [ImageRepository] for selection.
 */
class ImageIdentificationEditor : AbstractPropertyEditor() {

    private val comboBoxEditor = ComboBoxPropertyEditor()

    @Suppress("UNCHECKED_CAST")
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