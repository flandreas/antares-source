package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** A [BeanInfo] for [DipSwitchView].*/
@Suppress("unused")
class DipSwitchViewBeanInfo : DigitalComponentBeanInfo<DipSwitchView>() {

	companion object {
		private val name = PropertyImpl("element.property", String::class.java)
		private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
		private val initialValue = PropertyImpl("element.property.DipSwitch.initialValue", Long::class.java)
	}

	override fun addProperties(bean: DipSwitchView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		name.bind(editor, { bean.name }, { bean.name = it })
		bitWidth.bind(editor, { bean.bitWidth }, { bean.bitWidth = it!! })
		initialValue.bind(editor, { bean.initialValue }, { bean.initialValue = it!! })

		properties.add(name)
		properties.add(bitWidth)
		properties.add(initialValue)
	}
}