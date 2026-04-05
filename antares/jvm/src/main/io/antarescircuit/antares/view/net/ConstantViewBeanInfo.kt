package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.graph.model.param.ExpressionPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ConstantViewBeanInfo : DigitalComponentViewBeanInfo<ConstantView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val signalRep = AntaresProperties.signalRepresentation()
		private val value = ExpressionPropertySwing("value", "element.property.Constant.value", LongValue::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: ConstantView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(signalRep.bind(editor, beanIdProvider(bean.id), filter = { it != DigitalSignalRepresentation.FIXED_POINT }))
		properties.add(value.bind(editor, beanIdProvider(bean.id)))
	}
}