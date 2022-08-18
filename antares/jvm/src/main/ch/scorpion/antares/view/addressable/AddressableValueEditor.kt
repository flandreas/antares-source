package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.AddressableCellChange
import ch.scorpion.jabbah.base.logger
import ch.scorpion.antares.model.signal.BitOperation
import java.awt.Component
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*

class AddressableValueEditor(
	private val addressableRef: AddressableReference,
	private val addressableDisplayLayout: AddressableDisplayLayout,
	private val addressableCellChangeConsumer: (AddressableCellChange) -> Unit
) : DefaultCellEditor(JTextField()) {

	companion object {
		private val LOG by logger(AddressableValueEditor::class)
	}

	private var oldValue: ULong = 0UL
	private var row: Int = 0
	private var col: Int = 0

	private val textComponent: JTextField get() = component as JTextField

	init {
		clickCountToStart = 0

		textComponent.horizontalAlignment = SwingConstants.RIGHT

		component.addFocusListener(object : FocusAdapter() {
			override fun focusGained(e: FocusEvent?) {
				with (textComponent) {
					selectAll()
					oldValue = retrieveCurrentValue()
				}
			}

			override fun focusLost(e: FocusEvent?) {
				handleFocusLost()
			}
		})

		textComponent.inputVerifier = HexHumberInputVerifier()
	}

	override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
		this.row = row
		this.col = column
		return super.getTableCellEditorComponent(table, value, isSelected, row, column)
	}

	private fun retrieveCurrentValue(): ULong =
		addressableRef.addressable.dataAt(addressableDisplayLayout.getCellAddress(row, col))

	private fun handleFocusLost() {
		val newValue = retrieveCurrentValue()
		LOG.trace("Changed $row,$col from $oldValue to $newValue")
		if (newValue != oldValue) {
			addressableCellChangeConsumer(AddressableCellChange(addressableDisplayLayout.getCellAddress(row, col), oldValue, newValue))
		}
	}

	private inner class HexHumberInputVerifier : InputVerifier() {

		override fun verify(input: JComponent?): Boolean =
			BitOperation.normalizeHex(textComponent.text.trim(), addressableRef.addressable.dataWidth) != null
	}
}

