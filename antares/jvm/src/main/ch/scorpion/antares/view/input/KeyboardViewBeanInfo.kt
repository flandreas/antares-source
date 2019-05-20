package ch.scorpion.antares.view.input

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** A [BeanInfo] for [KeyboardView].*/
@Suppress("unused")class KeyboardViewBeanInfo : VerticeViewBeanInfo<KeyboardView>() {

	companion object {
		private val bufferSize = PropertyImpl("element.property.bufferSize", Int::class.java)
	}

	override fun addProperties(bean: KeyboardView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		bufferSize.bind(editor, { bean.bufferSize}, { bean.bufferSize = it!! })

		properties.add(bufferSize)
	}
}