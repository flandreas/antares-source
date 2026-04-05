package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class PowerOnResetViewBeanInfo : DigitalComponentViewBeanInfo<PowerOnResetView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val logic = CommandPropertySwing("logic", Logic.BASE_KEY, Logic::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: PowerOnResetView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(logic.bind(editor, beanIdProvider(bean.id)))
	}
}