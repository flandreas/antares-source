package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.drawingBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.graph.view.GraphProperties
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