package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.style.StyleType
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl

/**
 * A [BeanInfo] for [TextComponent].
 */
@Suppress("unused")
abstract class TextComponentBeanInfo<T : TextComponent>(
        private val fillAndStroke: Boolean = true
) : AbstractBeanInfo<T>() {

    companion object {
        private val filled = PropertyImpl("edit.property.filled", Boolean::class.java)
        private val stroked = PropertyImpl("edit.property.stroked", Boolean::class.java)
        private val styleType = PropertyImpl("graph.styleType", StyleType::class.java)
        private val color = PropertyImpl("edit.property.color", PredefinedColor::class.java)
        private val text = PropertyImpl("edit.property.TextComponent.text", String::class.java)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        if (fillAndStroke) {
            filled.bind(editor, { bean.filled }, { bean.filled = it!! })
            stroked.bind(editor, { bean.stroked }, { bean.stroked = it!! })
        }
        styleType.bind(editor, { bean.styleType }, { bean.styleType = it!! })
        color.bind(editor, { bean.customColor}, { bean.customColor = it })
        text.bind(editor, { bean.text }, { bean.text = it!! })

        if (fillAndStroke) {
            properties.add(filled)
            properties.add(stroked)
        }
        properties.add(styleType)
        properties.add(color)
        properties.add(text)
    }
}
@Suppress("unused")
class LabelComponentBeanInfo : TextComponentBeanInfo<LabelComponent>(fillAndStroke = false)

@Suppress("unused")
class TextComponentJvmBeanInfo : TextComponentBeanInfo<TextComponentJvm>()