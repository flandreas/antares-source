package ch.scorpion.jabbah.graph.view

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import javax.swing.JComboBox

class PortLabelPositionEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(PortLabelPosition.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<PortLabelPosition>()
    }
}