package io.antarescircuit.jabbah.edit.view

import io.antarescircuit.jabbah.base.swing.EnumRenderer
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import javax.swing.JComboBox

class SizeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(Size.entries.toTypedArray())
		(editor as JComboBox<Size>).renderer = EnumRenderer()
	}
}

class VerticalAlignmentEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(VerticalAlignment.entries.toTypedArray())
		(editor as JComboBox<VerticalAlignment>).renderer = EnumRenderer()
	}
}

class HorizontalAlignmentEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(HorizontalAlignment.entries.toTypedArray())
		(editor as JComboBox<HorizontalAlignment>).renderer = EnumRenderer()
	}
}