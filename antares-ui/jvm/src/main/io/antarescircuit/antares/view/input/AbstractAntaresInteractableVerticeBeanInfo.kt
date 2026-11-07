package io.antarescircuit.antares.view.input

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.view.GraphProperties
import io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView

abstract class AbstractAntaresInteractableVerticeBeanInfo<T: OrientableRectangularVerticeView<*>>
    : DigitalComponentViewBeanInfo<T>()
{
    companion object {
        private val interactablePropagationDelay = GraphProperties.interactivePropagationDelay()
    }

    override val isShowPropagationDelay: Boolean get() = false

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(interactablePropagationDelay.bind(editor, beanIdProvider(bean.id)))
    }
}