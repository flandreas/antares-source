package ch.scorpion.antares.view

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl

open class DigitalGateViewBeanInfo<T: AbstractDigitalGateView<*>> : DigitalComponentBeanInfo<T>() {

    companion object {
        val inputCount = PropertyImpl<InputCount>("element.property.inputCount", InputCount::class.java)
        val outputPortName = PropertyImpl<String>("element.property.outputPort", String::class.java)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        inputCount.bind(editor, {bean.chosenInputCount}, {bean.chosenInputCount = it!!}, !bean.model!!.isConnected, { it.ordinal >= 2})
        outputPortName.bind(editor, {bean.outputPortName }, {bean.outputPortName = it})
        properties.add(inputCount)
        properties.add(outputPortName)
    }
}