package ch.scorpion.antares.view

import ch.scorpion.antares.view.app.InputCountPropertySwing
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

open class DigitalGateViewBeanInfo<T: AbstractDigitalGateView<*>> : DigitalComponentBeanInfo<T>() {

    companion object {
	    private val inputCount = InputCountPropertySwing(componentBeanProvider)
	    private val outputPortName = CommandPropertySwing("outputPortName", "element.property.outputPort", String::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(inputCount.bind(editor, bean.id, editable = true, filter = { it.ordinal >= 2 }))
	    properties.add(outputPortName.bind(editor, bean.id))
    }
}