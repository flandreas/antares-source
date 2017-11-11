package ch.scorpion.jabbah.edit.model.rectangle

import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment


/** A [BeanInfo] for [RectangularComponent]. */
abstract class RectangularComponentBeanInfo<T: RectangularComponent> : AbstractBeanInfo<T>() {

    companion object {
        private val filled = PropertyImpl("edit.property.filled", Boolean::class.java)
        private val stroked = PropertyImpl("edit.property.stroked", Boolean::class.java)
        private val styleType = PropertyImpl("graph.styleType", StyleType::class.java)
        private val color = PropertyImpl("edit.property.color", PredefinedColor::class.java)
        private val text = PropertyImpl("edit.property.text", String::class.java)
        private val alignment = PropertyImpl("edit.property.verticalAlignment", VerticalAlignment::class.java)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        filled.bind(editor, { bean.filled }, { bean.filled = it!! })
        stroked.bind(editor, { bean.stroked }, { bean.stroked = it!! })
        styleType.bind(editor, { bean.styleType }, { bean.styleType = it!! })
        color.bind(editor, { bean.customColor}, { bean.customColor = it })
        text.bind(editor, { bean.text }, { bean.text = it!!} )
        alignment.bind(editor, { bean.alignment }, { bean.alignment = it!! })

        properties.add(filled)
        properties.add(stroked)
        properties.add(styleType)
        properties.add(color)
        properties.add(text)
        properties.add(alignment)
    }
}

@Suppress("unused")
class RectangleComponentBeanInfo() : RectangularComponentBeanInfo<RectangleComponent>()

@Suppress("unused")
class EllipseComponentBeanInfo() : RectangularComponentBeanInfo<EllipseComponent>()

@Suppress("unused")
class RoundRectangleComponentBeanInfo() : RectangularComponentBeanInfo<RoundRectangleComponent>()