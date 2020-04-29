package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** A [BeanInfo] for [ContainerDrawing].*/
class ContainerDrawingBeanInfo : AbstractBeanInfo<ContainerDrawing>() {

	companion object {
		private val execDrawScript = PropertyImpl("graph.property.ContainerDrawing.execDrawScript", ScriptProperty::class.java)
	}

	override fun addProperties(bean: ContainerDrawing, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		execDrawScript.bind(editor, { bean.execDrawScript }, { bean.execDrawScript = it!! })

		properties.add(execDrawScript)
	}
}