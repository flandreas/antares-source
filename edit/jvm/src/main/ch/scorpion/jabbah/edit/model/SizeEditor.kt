package ch.scorpion.jabbah.edit.model

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.edit.model.Size
import javax.swing.JComboBox

class SizeEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Size.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<Size>()
    }
}