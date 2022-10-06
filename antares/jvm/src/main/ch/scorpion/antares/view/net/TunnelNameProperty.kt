package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.net.TunnelName
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class TunnelNameProperty(
	val graph: DigitalGraph,
	propertyName: String,
	baseKey: String,
	beanProvider: BeanProvider
) : CommandPropertySwing<TunnelName>(propertyName, baseKey, TunnelName::class.java, beanProvider, propertyName, propertyName)

class TunnelNameEditor(
	graph: DigitalGraph
) : AbstractPropertyEditor() {

	private val comboBoxEditor = ComboBoxPropertyEditor()
	private val comboBox: JComboBox<TunnelName> get() = comboBoxEditor.customEditor as JComboBox<TunnelName>

	init {
		comboBox.model = DefaultComboBoxModel(graph.tunnelNames.toTypedArray())
		comboBox.isEditable = true
		editor = comboBox
	}

	override fun getValue(): Any? {
		val v = comboBox.editor.item
		return if (v is String) {
			TunnelName(v)
		} else {
			v
		}
	}

	override fun setValue(value: Any?) {
		comboBoxEditor.value = value
	}
}