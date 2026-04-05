package io.antarescircuit.antares.model.expression

import io.antarescircuit.jabbah.edit.properties.applicationDataBeanProvider
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
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