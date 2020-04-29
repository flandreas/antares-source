package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** A [BeanInfo] for [ControlViewComponent].*/
@Suppress("unused")
class ControlViewComponentBeanInfo : AbstractBeanInfo<ControlViewComponent>() {

	companion object {
		private val id = PropertyImpl("edit.property.id", String::class.java)
	}

	override fun addProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		id.bind(editor, { bean.controlView.controlName }, null, false)

		properties.add(id)
	}
}