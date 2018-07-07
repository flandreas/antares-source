package ch.scorpion.antares.view

import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.base.geom.Direction

/**
 * Base class for implementing {@link BeanInfo}s for {@link DigitalComponentView}s.
 */
open class DigitalComponentBeanInfo<T : DigitalComponentView<*>> : AbstractBeanInfo<T>() {
    companion object {
        val id = PropertyImpl("edit.property.id", Int::class.java)
        val propDelay = PropertyImpl("element.property.propagationDelay", Long::class.java)
        val orientation = PropertyImpl("edit.property.Component.orientation", Direction::class.java)
        val color = PropertyImpl("edit.property.color", PredefinedColor::class.java)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        id.bind(editor, {bean.id}, null, false)
        propDelay.bind(editor, {bean.propagationDelay}, {bean.propagationDelay = it!!})
        orientation.bind(editor, {bean.orientation}, {bean.orientation = it!!})
        color.bind(editor, {bean.customColor}, {bean.customColor = it})

        properties.add(id)
        if (isShowPropagationDelay) {
            properties.add(propDelay)
        }
        properties.add(orientation)
	    if (isShowColor) {
		    properties.add(color)
	    }
    }

    protected open var isShowPropagationDelay: Boolean = true
	protected open var isShowColor: Boolean = true
}