package io.antarescircuit.jabbah.base.geom

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import io.antarescircuit.jabbah.base.swing.EnumRenderer
import javax.swing.JComboBox

class DirectionEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Direction.entries.toTypedArray())
	    @Suppress("UNCHECKED_CAST")
	    (editor as JComboBox<Direction>).renderer = EnumRenderer()
    }
}