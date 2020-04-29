package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** Base class for implementing [BeanInfo]s for subclasses of [AbstractVerticeView]s*/
open class VerticeViewBeanInfo<T : AbstractVerticeView<*>> : ComponentBeanInfo<T>() {

	companion object {
		private val propDelay = PropertyImpl("element.property.propagationDelay", Long::class.java)
		private val color = PropertyImpl("edit.property.color", PredefinedColor::class.java)
		private val description = PropertyImpl("edit.property.description", TranslatableText::class.java)
		private val shadow = PropertyImpl("edit.property.shadow", Boolean::class.java)
	}

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		propDelay.bind(editor, { bean.propagationDelay }, { bean.propagationDelay = it!! })
		color.bind(editor, { bean.customColor }, { bean.customColor = it })
		shadow.bind(editor, { bean.shadow }, { bean.customShadow = it!! })
		description.bind(editor, { bean.description.translation }, { bean.description.translation = it!! })

		if (isShowPropagationDelay) {
			properties.add(propDelay)
		}
		properties.add(shadow)
		if (isShowColor) {
			properties.add(color)
		}
		properties.add(description)
	}

	protected open var isShowPropagationDelay: Boolean = true
	protected open var isShowColor: Boolean = true

}