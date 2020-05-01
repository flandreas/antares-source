package ch.scorpion.antares.view.input

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class KeyboardViewBeanInfo : VerticeViewBeanInfo<KeyboardView>() {

	companion object {
		private val bufferSize = PropertyImpl("bufferSize", "element.property.bufferSize", Int::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: KeyboardView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bufferSize.bind(editor, bean.id))
	}
}