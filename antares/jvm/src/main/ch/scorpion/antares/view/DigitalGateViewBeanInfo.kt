package ch.scorpion.antares.view

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyImpl

open class DigitalGateViewBeanInfo<T: AbstractDigitalGateView<*>> : DigitalComponentBeanInfo<T>() {

    companion object {
	    private val inputCount = PropertyImpl("chosenInputCount", "element.property.inputCount", InputCount::class.java, componentBeanProvider)
	    private val outputPortName = PropertyImpl("outputPortName", "element.property.outputPort", String::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(inputCount.bind(editor, bean.id, editable = !bean.model.isConnected, filter = { it.ordinal >= 2 }))
	    properties.add(outputPortName.bind(editor, bean.id))
    }
}