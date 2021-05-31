package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ControlViewComponentBeanInfo : AbstractBeanInfo<ControlViewComponent>() {

	companion object {
		private val id = EditProperties.id()
		private val modelId = GraphProperties.modelId()
	}

	override fun addProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(id.bind(editor, bean.id, editable = false))
		properties.add(modelId.bind(editor, bean.modelId, editable = false))
	}
}