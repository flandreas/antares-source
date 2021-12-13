package ch.scorpion.antares.model

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.graph.model.param.GraphParamValueEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class BitWidthGraphParamValueEditor : JComboBox<BitWidth>(), GraphParamValueEditor {

	init {
		model = DefaultComboBoxModel(BitWidth.PREDEFINED.toTypedArray())
	}

	override var value: Any
		get() = selectedItem
		set(value) {
			selectedItem = value
		}

	override var changeHandler: (() -> Unit)? = null
		set(value) {
			field = value
			if (value != null) {
				addActionListener { value.invoke() }
			}
		}

	override var editorEnabled: Boolean
		get() = isEnabled
		set(value) { isEnabled = value }

}