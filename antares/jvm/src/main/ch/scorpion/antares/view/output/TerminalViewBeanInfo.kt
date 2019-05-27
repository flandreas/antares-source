package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TerminalViewBeanInfo : VerticeViewBeanInfo<TerminalView>() {

	companion object {
		private val rowsCount = PropertyImpl("element.property.Terminal.rowsCount", Int::class.java)
		private val columnsCount = PropertyImpl("element.property.Terminal.columnsCount", Int::class.java)
		private val size = PropertyImpl("edit.property.size", Size::class.java)
	}

	override fun addProperties(bean: TerminalView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		rowsCount.bind(editor, { bean.rowsCount }, { bean.rowsCount = it!! })
		columnsCount.bind(editor, { bean.columnsCount }, { bean.columnsCount = it!! })
		size.bind(editor, { bean.size }, { bean.size = it!! })


		properties.add(rowsCount)
		properties.add(columnsCount)
		properties.add(size)
	}
}