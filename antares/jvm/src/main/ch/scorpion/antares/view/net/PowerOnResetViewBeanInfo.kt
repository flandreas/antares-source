package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class PowerOnResetViewBeanInfo : DigitalComponentBeanInfo<PowerOnResetView>() {

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