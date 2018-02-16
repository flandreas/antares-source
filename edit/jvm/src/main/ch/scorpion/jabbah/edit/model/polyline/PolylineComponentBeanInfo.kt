package ch.scorpion.jabbah.edit.model.polyline

import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl


/**
 * A [BeanInfo] for [PolylineComponent].
 */
@Suppress("unused")
class PolylineComponentBeanInfo : AbstractBeanInfo<PolylineComponent>() {

    companion object {
        private val filled = PropertyImpl("edit.property.filled", Boolean::class.java)
        private val styleType = PropertyImpl("draw.styleType", StyleType::class.java)
        private val color = PropertyImpl("edit.property.color", PredefinedColor::class.java)
    }

    override fun addProperties(bean: PolylineComponent, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        filled.bind(editor, { bean.filled }, { bean.filled = it!! })
        styleType.bind(editor, { bean.styleType }, { bean.styleType = it!! })
        color.bind(editor, { bean.customColor }, { bean.customColor = it })

        properties.add(filled)
        properties.add(styleType)
        properties.add(color)
    }
}