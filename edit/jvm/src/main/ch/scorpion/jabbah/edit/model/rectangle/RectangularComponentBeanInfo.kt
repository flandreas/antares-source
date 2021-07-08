package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo


/** A [BeanInfo] for [RectangularComponent]. */
abstract class RectangularComponentBeanInfo<T: RectangularComponent> : ComponentBeanInfo<T>() {

    companion object {
	    private val filled = EditProperties.filled()
        private val stroked = EditProperties.stroked()
        private val styleType = EditProperties.styleType()
        private val color = EditProperties.color()
	    private val stroke = EditProperties.stroke()
        private val text = EditProperties.translatableText()
        private val verticalAlignment = EditProperties.verticalAlignment()
	    private val horizontalAlignment = EditProperties.horizontalAlignment()
	    private val shadow = EditProperties.shadow()
	    private val description = EditProperties.description()
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        properties.add(filled.bind(editor, bean.id))
        properties.add(stroked.bind(editor, bean.id))
	    properties.add(shadow.bind(editor, bean.id))
        properties.add(styleType.bind(editor, bean.id))
        properties.add(color.bind(editor, bean.id))
	    properties.add(stroke.bind(editor, bean.id))
        properties.add(text.bind(editor, bean.id, filter = { false }))
	    properties.add(description.bind(editor, bean.id))
        properties.add(verticalAlignment.bind(editor, bean.id))
        properties.add(horizontalAlignment.bind(editor, bean.id))
    }
}

@Suppress("unused")
class RectangleComponentBeanInfo : RectangularComponentBeanInfo<RectangleComponent>()

@Suppress("unused")
class EllipseComponentBeanInfo : RectangularComponentBeanInfo<EllipseComponent>()

@Suppress("unused")
class RoundRectangleComponentBeanInfo : RectangularComponentBeanInfo<RoundRectangleComponent>()