package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ClockViewBeanInfo : DigitalComponentBeanInfo<ClockView>() {

    companion object {
	    private val period = PropertyImpl("period", "element.property.ClockView.period", Long::class.java, componentBeanProvider)
	    private val enabled = PropertyImpl("enabled", "element.property.ClockView.enabled", Boolean::class.java, componentBeanProvider)
	    private val knobEnabled = PropertyImpl("knobEnabled", "element.property.ClockView.knobEnabled", Boolean::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: ClockView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(period.bind(editor, bean.id))
	    properties.add(enabled.bind(editor, bean.id))
	    properties.add(knobEnabled.bind(editor, bean.id))
    }

    override var isShowPropagationDelay: Boolean
        get() = false
        set(value) { super.isShowPropagationDelay = value}
}