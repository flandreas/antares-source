package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ClockViewBeanInfo : DigitalComponentViewBeanInfo<ClockView>() {

    companion object {
	    private val periodOrFrequency = CommandPropertySwing("periodOrFrequency", "element.property.ClockView.periodOrFrequency", String::class.java, componentBeanProvider)
	    private val enabled = CommandPropertySwing("enabled", "element.property.ClockView.enabled", Boolean::class.java, componentBeanProvider)
	    private val knobEnabled = CommandPropertySwing("knobEnabled", "element.property.ClockView.knobEnabled", Boolean::class.java, componentBeanProvider)
		private val offPercentage = CommandPropertySwing("offPercentage", "element.property.ClockView.offPercentage", Double::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: ClockView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(periodOrFrequency.bind(editor, beanIdProvider(bean.id)))
		properties.add(offPercentage.bind(editor, beanIdProvider(bean.id)))
	    properties.add(enabled.bind(editor, beanIdProvider(bean.id)))
	    properties.add(knobEnabled.bind(editor, beanIdProvider(bean.id)))
    }

    override val isShowPropagationDelay: Boolean get() = false
}