package ch.scorpion.jabbah.edit.properties

import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.edit.model.image.ImageRepository
import ch.scorpion.jabbah.edit.module.EditModule
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

/**
 * Offers all [ImageIdentification]s of the current [ImageRepository] for selection.
 */
class ImageIdentificationEditor : AbstractPropertyEditor() {

    private val comboBoxEditor = ComboBoxPropertyEditor()
    private val comboBox: JComboBox<ImageIdentification> get() = comboBoxEditor.customEditor as JComboBox<ImageIdentification>

    init {
        comboBox.model = DefaultComboBoxModel(
            EditModule.imageRepository.getAllImageIds().toTypedArray()
        )
        // TODO Empty value
        //(comboBox.model as DefaultComboBoxModel).insertElementAt(null, 0)
        comboBox.isEditable = true
        editor = comboBox
    }

    override fun getValue(): Any = comboBox.editor.item

    override fun setValue(value: Any?) {
        comboBoxEditor.value = value
    }
}