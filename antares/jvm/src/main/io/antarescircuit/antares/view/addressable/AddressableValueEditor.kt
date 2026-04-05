package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.AddressableReference
import io.antarescircuit.jabbah.base.System
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*

class AddressableValueEditor(
	private val addressableRef: AddressableReference,
	private val converterProvider: () -> AddressableValueConverter,
) : DefaultCellEditor(JTextField()) {

	private val textComponent: JTextField get() = component as JTextField

	init {
		clickCountToStart = 0

		textComponent.horizontalAlignment = SwingConstants.RIGHT

		component.addFocusListener(object : FocusAdapter() {
			override fun focusGained(e: FocusEvent?) {
				with (textComponent) {
					selectAll()
				}
			}
		})

		textComponent.inputVerifier = HexHumberInputVerifier()
	}

	private inner class HexHumberInputVerifier : InputVerifier() {

		override fun verify(input: JComponent?): Boolean {
			val valid = converterProvider().parse(textComponent.text.trim(), addressableRef.addressable.dataWidth) != null
			if (!valid) {
				System.beep()
			}
			return valid
		}
	}
}

