package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import com.l2fprod.common.propertysheet.Property

/**
 * A [BeanInfo] for [ConstantView].
 */
class ConstantViewBeanInfo : DigitalComponentBeanInfo<ConstantView>() {

	companion object {
		private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
		private val signalRep = PropertyImpl("element.property.DigitalSignalRepresentation", DigitalSignalRepresentation::class.java)
		private val value = PropertyImpl("element.property.Constant.value", Long::class.java)
	}

	override fun addProperties(bean: ConstantView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		bitWidth.bind(editor, { bean.bitWidth }, { bean.bitWidth = it!! })
		signalRep.bind(editor, { bean.signalRepresentation }, { bean.signalRepresentation = it!! })
		value.bind(editor, { bean.value }, { bean.value = it!! })

		properties.add(bitWidth)
		properties.add(signalRep)
		properties.add(value)
	}
}