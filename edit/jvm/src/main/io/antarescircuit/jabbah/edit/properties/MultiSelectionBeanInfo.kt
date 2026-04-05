package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

class MultiSelectionBeanInfo(
	private val delegate: AbstractBeanInfo<Any>
) : AbstractBeanInfo<MultiSelection>() {

	override fun addProperties(bean: MultiSelection, editor: Editor, properties: MutableList<Property>) =
		delegate.addProperties(bean.selection.first().propertyOwner, editor, properties)

	override fun getProperties(bean: MultiSelection, editor: Editor): Array<Property> =
		super.getProperties(bean, editor).filter {
			it !is AbstractReflectionPropertySwing<*> || (it.editable && it.supportMultiSelection)
		}.toTypedArray()
}