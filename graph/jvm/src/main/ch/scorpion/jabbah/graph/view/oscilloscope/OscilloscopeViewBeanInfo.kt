package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class OscilloscopeViewBeanInfo : AbstractBeanInfo<OscilloscopeView>() {

    companion object {
        private val id = PropertyImpl("edit.property.id", Int::class.java)
        private val scale = PropertyImpl("graph.property.oscilloscopeScale", Double::class.java)
    }

    override fun addProperties(bean: OscilloscopeView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        id.bind(editor, { bean.id }, null, false)
        scale.bind(editor, { bean.timelineScale}, {bean.timelineScale = it!! })

        properties.add(id)
        properties.add(scale)
    }
}