package ch.scorpion.antares.view.signal

import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** A [BeanInfo] for [DigitalSignalSourceControlView]. */
@Suppress("unused")
class DigitalSignalSourceControlViewBeanInfo : AbstractBeanInfo<DigitalSignalSourceControlView<*>>() {

	companion object {
		private val name = PropertyImpl("graph.property.label", String::class.java)
	}

	override fun addProperties(bean: DigitalSignalSourceControlView<*>, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		name.bind(editor, { bean.name }, null, false)

		properties.add(name)
	}
}