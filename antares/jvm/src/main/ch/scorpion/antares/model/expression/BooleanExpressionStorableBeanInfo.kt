package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.app.properties.applicationDataBeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class BooleanExpressionStorableBeanInfo : AbstractBeanInfo<BooleanExpressionStorable>() {

    companion object {
        private val name = EditProperties.name(beanProvider = applicationDataBeanProvider)
    }

    override fun addProperties(bean: BooleanExpressionStorable, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(name.bind(editor, emptyList()))
    }
}