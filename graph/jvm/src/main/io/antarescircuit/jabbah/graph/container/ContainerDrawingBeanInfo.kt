package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.drawingBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ContainerDrawingBeanInfo : AbstractBeanInfo<ContainerDrawing>() {

	companion object {
		private val controlViewVisibility = GraphProperties.controlViewVisibility(beanProvider = drawingBeanProvider)
	}

	override fun addProperties(bean: ContainerDrawing, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val execDrawScript = EditProperties.script("execDrawScript", "graph.property.ContainerDrawing.execDrawScript",
			beanProvider = drawingBeanProvider, bean::createDrawSymbolScriptParser, ContainerDrawing.SCRIPT_HELP_ID)

		properties.add(execDrawScript.bind(editor, listOf()))
		properties.add(controlViewVisibility.bind(editor, listOf()))
	}
}