package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class OscilloscopeProbeVerticeViewBeanInfo : ComponentBeanInfo<OscilloscopeProbeVerticeView<Any>>() {

	companion object {
		private val name = EditProperties.untranslatableName()
	}

	override fun addProperties(bean: OscilloscopeProbeVerticeView<Any>, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(name.bind(editor, bean.id))
	}
}