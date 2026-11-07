package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.EnterBehavior
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class KeyboardViewBeanInfo : VerticeViewBeanInfo<KeyboardView>() {

	companion object {
		private val bufferSize = CommandPropertySwing("bufferSize", "element.property.bufferSize", Int::class.java, componentBeanProvider)
		private val enterBehavior = CommandPropertySwing("enterBehavior", "element.property.Keyboard.enterBehavior", EnterBehavior::class.java, componentBeanProvider)
 	}

	override fun addProperties(bean: KeyboardView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bufferSize.bind(editor, beanIdProvider(bean.id)))
		properties.add(enterBehavior.bind(editor, beanIdProvider(bean.id)))
	}
}