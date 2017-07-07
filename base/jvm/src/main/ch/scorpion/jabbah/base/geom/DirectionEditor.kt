package ch.scorpion.jabbah.base.geom

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.swing.EnumRenderer
import javax.swing.JComboBox

class DirectionEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Direction.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<Direction>()
    }
}