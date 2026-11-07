package io.antarescircuit.antares.view.addressable

import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.DefaultCellEditor
import javax.swing.JTextField
import javax.swing.SwingConstants

class AddressableCommentEditor() : DefaultCellEditor(JTextField()) {

	private val textComponent: JTextField get() = component as JTextField

	init {
		clickCountToStart = 0

		textComponent.horizontalAlignment = SwingConstants.LEFT

		component.addFocusListener(object : FocusAdapter() {
			override fun focusGained(e: FocusEvent?) {
				with (textComponent) {
					selectAll()
				}
			}
		})
	}
}