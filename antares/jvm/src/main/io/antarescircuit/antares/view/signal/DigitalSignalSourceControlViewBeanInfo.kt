package io.antarescircuit.antares.view.signal

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class DigitalSignalSourceControlViewBeanInfo : AbstractBeanInfo<DigitalSignalSourceControlView<*>>() {

	companion object {
		private val name = EditProperties.untranslatableName()
	}

	override fun addProperties(bean: DigitalSignalSourceControlView<*>, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(name.bind(editor, beanIdProvider(bean.id)))
	}
}