package io.antarescircuit.antares.model

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.graph.model.param.GraphParamValueEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JComponent

class BitWidthGraphParamValueEditor : JComboBox<BitWidth>(), GraphParamValueEditor {

	init {
		model = DefaultComboBoxModel(BitWidth.PREDEFINED.toTypedArray())
	}

	override var paramValue: Any
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

	@Suppress("PROPERTY_HIDES_JAVA_FIELD")
	override val editor: JComponent get() = this
}