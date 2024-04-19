package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.AddressableCommentChange
import ch.scorpion.antares.model.addressable.AddressableReference
import java.awt.Component
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.DefaultCellEditor
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.SwingConstants

class AddressableCommentEditor(
	private val addressableRef: AddressableReference,
	private val addressableDisplayLayout: AddressableDisplayLayout,
	private val addressableCommentChangeConsumer: (AddressableCommentChange) -> Unit
) : DefaultCellEditor(JTextField()) {

	private var oldValue: String? = null
	private var row: Int = 0
	private var col: Int = 0

	private val textComponent: JTextField get() = component as JTextField

	init {
		clickCountToStart = 0

		textComponent.horizontalAlignment = SwingConstants.LEFT

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
	}

	override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
		this.row = row
		this.col = column
		return super.getTableCellEditorComponent(table, value, isSelected, row, column)
	}

	private fun retrieveCurrentValue(): String? =
		addressableRef.addressable.commentAt(addressableDisplayLayout.getCellAddress(row, col))

	private fun handleFocusLost() {
		val newValue = retrieveCurrentValue()
		if (newValue != oldValue) {
			addressableCommentChangeConsumer(AddressableCommentChange(addressableDisplayLayout.getCellAddress(row, col), oldValue, newValue))
		}
	}

}