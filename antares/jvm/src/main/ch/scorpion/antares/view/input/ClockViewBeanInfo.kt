package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl

/**
 * A [BeanInfo] for [ClockView].
 */
class ClockViewBeanInfo : DigitalComponentBeanInfo<ClockView>() {

    companion object {
        private val period = PropertyImpl("element.property.ClockView.period", Long::class.java)
        private val enabled = PropertyImpl("element.property.ClockView.enabled", Boolean::class.java)
    }

    override fun addProperties(bean: ClockView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        period.bind(editor, { bean.period }) { bean.period = it!! }
        enabled.bind(editor, { bean.isEnabled }) { bean.isEnabled = it!! }

        properties.add(period)
        properties.add(enabled)
    }

    override var isShowPropagationDelay: Boolean
        get() = true
        set(value) { super.isShowPropagationDelay = value}
}