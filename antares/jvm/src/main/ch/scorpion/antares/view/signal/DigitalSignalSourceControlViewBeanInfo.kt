package ch.scorpion.antares.view.signal

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class DigitalSignalSourceControlViewBeanInfo : AbstractBeanInfo<DigitalSignalSourceControlView<*>>() {

	companion object {
		private val name = EditProperties.untranslatableName()
	}

	override fun addProperties(bean: DigitalSignalSourceControlView<*>, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(name.bind(editor, bean.id))
	}
}