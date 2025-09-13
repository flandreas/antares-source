package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.graph.model.param.ExpressionPropertySwing
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