package ch.scorpion.antares.view

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.edit.ComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import com.l2fprod.common.propertysheet.Property

/**
 * Base class for implementing {@link BeanInfo}s for {@link DigitalComponentView}s.
 */
open class DigitalComponentBeanInfo<T : DigitalComponentView<*>> : ComponentBeanInfo<T>() {
	companion object {
		private val propDelay = PropertyImpl("element.property.propagationDelay", Long::class.java)
		private val orientation = PropertyImpl("edit.property.Component.orientation", Direction::class.java)
		private val color = PropertyImpl("edit.property.color", PredefinedColor::class.java)
		private val customDescription = PropertyImpl("edit.property.description", TranslatableText::class.java)
		private val shadow = PropertyImpl("edit.property.shadow", Boolean::class.java)
	}

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		propDelay.bind(editor, { bean.propagationDelay }, { bean.propagationDelay = it!! })
		orientation.bind(editor, { bean.orientation }, { bean.orientation = it!! })
		color.bind(editor, { bean.customColor }, { bean.customColor = it })
		shadow.bind(editor, { bean.shadow }, { bean.customShadow = it!! })
		customDescription.bind(editor, { bean.customDescription.translation }, { bean.customDescription.translation = it!! })

		if (isShowPropagationDelay) {
			properties.add(propDelay)
		}
		properties.add(shadow)
		properties.add(orientation)
		if (isShowColor) {
			properties.add(color)
		}
		properties.add(customDescription)
	}

	protected open var isShowPropagationDelay: Boolean = true
	protected open var isShowColor: Boolean = true
}