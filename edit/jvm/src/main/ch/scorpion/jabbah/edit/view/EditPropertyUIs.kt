package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import javax.swing.JComboBox

class SizeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(Size.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<Size>()
	}
}

class VerticalAlignmentEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(VerticalAlignment.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<VerticalAlignment>()
	}
}

class HorizontalAlignmentEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(HorizontalAlignment.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<HorizontalAlignment>()
	}
}