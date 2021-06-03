package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TerminalViewBeanInfo : VerticeViewBeanInfo<TerminalView>() {

	companion object {
		private val rowsCount = CommandPropertySwing("rowsCount", "element.property.Terminal.rowsCount", Int::class.java, componentBeanProvider)
		private val columnsCount = CommandPropertySwing("columnsCount", "element.property.Terminal.columnsCount", Int::class.java, componentBeanProvider)
		private val size = EditProperties.size()
		private val lightColor = AntaresProperties.lightColor(baseKey = "element.property.Terminal.textColor")
	}

	override fun addProperties(bean: TerminalView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(rowsCount.bind(editor, bean.id))
		properties.add(columnsCount.bind(editor, bean.id))
		properties.add(size.bind(editor, bean.id))
		properties.add(lightColor.bind(editor, bean.id, optional = true))
	}
}