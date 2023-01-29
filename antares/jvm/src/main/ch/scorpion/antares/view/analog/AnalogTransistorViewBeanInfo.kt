package ch.scorpion.antares.view.analog

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.Handedness
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class AnalogTransistorViewBeanInfo : AnalogComponentViewBeanInfo<AnalogTransistorView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val transistorType = AntaresProperties.transistorType()
		private val transistorSymbol = AntaresProperties.transistorSymbol()
		private val handedness = AntaresProperties.handedness(baseKey = Handedness.BASE_KEY)
	}

	override fun addProperties(bean: AnalogTransistorView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(transistorType.bind(editor, beanIdProvider(bean.id)))
		properties.add(transistorSymbol.bind(editor, beanIdProvider(bean.id)))
		properties.add(handedness.bind(editor, beanIdProvider(bean.id)))
	}
}