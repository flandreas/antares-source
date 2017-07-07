package ch.scorpion.jabbah.graph.view

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.graph.model.PortType
import javax.swing.JComboBox

class PortTypeEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(PortType.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<PortType>()
    }
}