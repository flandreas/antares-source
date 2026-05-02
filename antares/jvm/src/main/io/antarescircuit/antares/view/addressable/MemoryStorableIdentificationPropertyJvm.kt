package io.antarescircuit.antares.view.addressable

import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import io.antarescircuit.antares.model.addressable.MemoryStorableIdentification
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.swing.ToStringRenderer
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class MemoryStorableIdentificationRenderer : ToStringRenderer<MemoryStorableIdentification>(
    Translations.getString("element.property.memoryStorable.none.name")
)

class MemoryStorableIdentificationEditor : AbstractPropertyEditor() {

    private val comboBoxEditor = ComboBoxPropertyEditor()

    @Suppress("UNCHECKED_CAST")
    private val comboBox: JComboBox<MemoryStorableIdentification> get() =
        comboBoxEditor.customEditor as JComboBox<MemoryStorableIdentification>

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