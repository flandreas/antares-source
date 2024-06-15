package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
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