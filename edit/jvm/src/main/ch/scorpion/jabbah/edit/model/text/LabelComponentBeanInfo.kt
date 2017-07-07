package ch.scorpion.jabbah.edit.model.text

import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl


/**
 * A [BeanInfo] for [LabelComponent].
 */
class LabelComponentBeanInfo : AbstractBeanInfo<LabelComponent>() {

    companion object {
        private val text = PropertyImpl("edit.property.LabelComponent.text", String::class.java)
    }

    override fun addProperties(bean: LabelComponent, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        text.bind(editor, { bean.text }, { bean.text = it!! })
        properties.add(text)
    }
}