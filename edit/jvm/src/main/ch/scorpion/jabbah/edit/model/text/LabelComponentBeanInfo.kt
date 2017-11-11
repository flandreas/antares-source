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
abstract class TextComponentBeanInfo<T : TextComponent> : AbstractBeanInfo<T>() {

    companion object {
        private val filled = PropertyImpl("edit.property.filled", Boolean::class.java)
        private val styleType = PropertyImpl("graph.styleType", StyleType::class.java)
        private val color = PropertyImpl("edit.property.color", PredefinedColor::class.java)
        private val text = PropertyImpl("edit.property.TextComponent.text", String::class.java)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        filled.bind(editor, { bean.filled }, { bean.filled = it!! })
        styleType.bind(editor, { bean.styleType }, { bean.styleType = it!! })
        color.bind(editor, { bean.customColor}, { bean.customColor = it })
        text.bind(editor, { bean.text }, { bean.text = it!! })

        properties.add(filled)
        properties.add(styleType)
        properties.add(color)
        properties.add(text)
    }
}
@Suppress("unused")
class LabelComponentBeanInfo : TextComponentBeanInfo<LabelComponent>()

@Suppress("unused")
class TextComponentJvmBeanInfo : TextComponentBeanInfo<TextComponentJvm>()