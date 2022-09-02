package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SixteenSegmentDisplayViewBeanInfo : VerticeViewBeanInfo<SixteenSegmentDisplayView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val lightColor = AntaresProperties.lightColor()
		private val size = EditProperties.size()
		private val hasBorder = EditProperties.border()
	}

	init {
		isShowColor = false
	}

	override fun addProperties(bean: SixteenSegmentDisplayView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val connected = bean.model.isConnected

		properties.add(name.bind(editor, bean.id))
		properties.add(lightColor.bind(editor, bean.id))
		properties.add(size.bind(editor, bean.id, editable = !connected))
		properties.add(hasBorder.bind(editor, bean.id))
	}
}