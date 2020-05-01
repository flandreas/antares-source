package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.drawingBeanProvider
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ContainerDrawingBeanInfo : AbstractBeanInfo<ContainerDrawing>() {

	companion object {
		private val execDrawScript = PropertyImpl("execDrawScript", "graph.property.ContainerDrawing.execDrawScript", ScriptProperty::class.java, drawingBeanProvider)
	}

	override fun addProperties(bean: ContainerDrawing, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(execDrawScript.bind(editor, listOf()))
	}
}