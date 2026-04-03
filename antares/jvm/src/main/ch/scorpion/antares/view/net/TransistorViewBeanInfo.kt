package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.antares.view.Handedness
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TransistorViewBeanInfo : DigitalComponentViewBeanInfo<TransistorView>() {

	companion object {
		private val transistorType = AntaresProperties.transistorType()
		private val transistorSymbol = AntaresProperties.transistorSymbol()
		private val bitWidth = AntaresProperties.bitWidth()
		private val handedness = AntaresProperties.handedness(baseKey = Handedness.BASE_KEY)
	}

	override fun addProperties(bean: TransistorView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(transistorType.bind(editor, beanIdProvider(bean.id)))
		properties.add(transistorSymbol.bind(editor, beanIdProvider(bean.id)))
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(handedness.bind(editor, beanIdProvider(bean.id)))
	}
}