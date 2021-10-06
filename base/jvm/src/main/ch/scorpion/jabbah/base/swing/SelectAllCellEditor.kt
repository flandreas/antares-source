package ch.scorpion.jabbah.base.swing

import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.DefaultCellEditor
import javax.swing.JTextField

class SelectAllCellEditor(
	private val textField: JTextField
) : DefaultCellEditor(textField) {

	init {
		clickCountToStart = 0
		textField.addFocusListener(object : FocusAdapter(){
			override fun focusGained(e: FocusEvent?) {
				textField.selectAll()
			}
		})
	}
}