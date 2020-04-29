package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/**
 * A [BeanInfo] for [ClockView].
 */
@Suppress("unused")
class ClockViewBeanInfo : DigitalComponentBeanInfo<ClockView>() {

    companion object {
        private val period = PropertyImpl("element.property.ClockView.period", Long::class.java)
        private val enabled = PropertyImpl("element.property.ClockView.enabled", Boolean::class.java)
	    private val knobEnabled = PropertyImpl("element.property.ClockView.knobEnabled", Boolean::class.java)
    }

    override fun addProperties(bean: ClockView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        period.bind(editor, { bean.period }) { bean.period = it!! }
        enabled.bind(editor, { bean.isEnabled }) { bean.isEnabled = it!! }
	    knobEnabled.bind(editor, { bean.isKnobEnabled }, { bean.isKnobEnabled = it!! })

        properties.add(period)
        properties.add(enabled)
	    properties.add(knobEnabled)
    }

    override var isShowPropagationDelay: Boolean
        get() = false
        set(value) { super.isShowPropagationDelay = value}
}