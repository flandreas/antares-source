package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import com.l2fprod.common.propertysheet.Property

abstract class AbstractCurveBeanInfo<T: AbstractCurveComponent> : AbstractComponentBeanInfo<T>() {

    companion object {
        private val filled = EditProperties.filled()
        private val styleType = EditProperties.styleType()
        private val color = EditProperties.color()
        private val stroke = EditProperties.stroke()
        private val shadow = EditProperties.shadow()
        private val mirrorH = EditProperties.horizontallyMirrored()
        private val mirrorV = EditProperties.verticallyMirrored()
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        properties.add(filled.bind(editor, beanIdProvider(bean.id)))
        properties.add(styleType.bind(editor, beanIdProvider(bean.id)))
        properties.add(color.bind(editor, beanIdProvider(bean.id)))
        properties.add(stroke.bind(editor, beanIdProvider(bean.id)))
        properties.add(shadow.bind(editor, beanIdProvider(bean.id)))

        properties.add(mirrorH.bind(editor, beanIdProvider(bean.id)))
        properties.add(mirrorV.bind(editor, beanIdProvider(bean.id)))
    }
}