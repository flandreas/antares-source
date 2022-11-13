package ch.scorpion.antares.view

import ch.scorpion.antares.view.app.InputCountPropertySwing
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.antares.view.gate.AbstractLogicGateView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

open class DigitalGateViewBeanInfo<T: AbstractDigitalGateView<*>> : DigitalComponentBeanInfo<T>() {

    companion object {
	    private val inputCount = InputCountPropertySwing(componentBeanProvider)
	    private val bitWidth = AntaresProperties.bitWidth()
	    private val outputPortName = CommandPropertySwing("outputPortName", AbstractDigitalGateView.BASE_KEY_OUTPUT_PORT_NAME, String::class.java, componentBeanProvider)
	    private val negateInput = Array(8) { portId ->
		    CommandPropertySwing("negateInput${portId + 1}", "${AbstractLogicGateView.BASE_KEY_NEGATE_INPUT}${portId + 1}", Boolean::class.java, componentBeanProvider)
	    }
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(inputCount.bind(editor, beanIdProvider(bean.id), editable = true, filter = { it.ordinal >= 2 }))
	    properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
	    properties.add(outputPortName.bind(editor, beanIdProvider(bean.id)))

	    for (i in 0 until bean.chosenInputCount.count) {
		    properties.add(negateInput[i].bind(editor, beanIdProvider(bean.id)))
	    }
    }
}