package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class BreakViewBeanInfo : DigitalComponentBeanInfo<BreakView>() {
	companion object {
		val logic = CommandPropertySwing("logic", "library.element.Break.logic", Logic::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: BreakView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(logic.bind(editor, bean.id))
	}
}