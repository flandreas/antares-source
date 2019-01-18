package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.edit.ComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class OscilloscopeViewBeanInfo : ComponentBeanInfo<OscilloscopeView>() {

    companion object {
        private val scale = PropertyImpl("graph.property.oscilloscopeScale", Double::class.java)
    }

    override fun addProperties(bean: OscilloscopeView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        scale.bind(editor, { bean.timelineScale}, {bean.timelineScale = it!! })
        properties.add(scale)
    }
}