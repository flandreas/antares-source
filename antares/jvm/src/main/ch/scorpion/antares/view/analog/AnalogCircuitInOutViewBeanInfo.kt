package ch.scorpion.antares.view.analog

import ch.scorpion.antares.view.inout.AbstractCircuitInOutViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class AnalogCircuitInOutViewBeanInfo : AbstractCircuitInOutViewBeanInfo<AnalogCircuitInOutView>() {
    companion object {
        private val description = EditProperties.description()
    }

    override fun addProperties(bean: AnalogCircuitInOutView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(description.bind(editor, beanIdProvider(bean.id)))
    }
}