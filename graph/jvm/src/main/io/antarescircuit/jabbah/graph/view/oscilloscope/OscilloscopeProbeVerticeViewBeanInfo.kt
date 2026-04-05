package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
open class OscilloscopeProbeVerticeViewBeanInfo : AbstractComponentBeanInfo<OscilloscopeProbeVerticeView<Any>>() {

	companion object {
		private val name = EditProperties.untranslatableName()
	}

	override fun addProperties(bean: OscilloscopeProbeVerticeView<Any>, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(name.bind(editor, beanIdProvider(bean.id)))
	}
}