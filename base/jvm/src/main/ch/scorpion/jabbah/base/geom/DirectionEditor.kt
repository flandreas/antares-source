package ch.scorpion.jabbah.base.geom

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.swing.EnumRenderer
import javax.swing.JComboBox

class DirectionEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Direction.entries.toTypedArray())
	    @Suppress("UNCHECKED_CAST")
	    (editor as JComboBox<Direction>).renderer = EnumRenderer()
    }
}