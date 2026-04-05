package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TerminalViewBeanInfo : VerticeViewBeanInfo<TerminalView>() {

	companion object {
		private val rowsCount = CommandPropertySwing("rowsCount", "element.property.Terminal.rowsCount", Int::class.java, componentBeanProvider)
		private val columnsCount = CommandPropertySwing("columnsCount", "element.property.Terminal.columnsCount", Int::class.java, componentBeanProvider)
		private val size = EditProperties.size()
		private val lightColor = AntaresProperties.lightColor(baseKey = "element.property.Terminal.textColor")
		private val handedness = AntaresProperties.handedness(baseKey = "element.property.Terminal.handedness")
	}

	override fun addProperties(bean: TerminalView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(rowsCount.bind(editor, beanIdProvider(bean.id)))
		properties.add(columnsCount.bind(editor, beanIdProvider(bean.id)))
		properties.add(size.bind(editor, beanIdProvider(bean.id)))
		properties.add(lightColor.bind(editor, beanIdProvider(bean.id), optional = true))
		properties.add(handedness.bind(editor, beanIdProvider(bean.id)))
	}
}