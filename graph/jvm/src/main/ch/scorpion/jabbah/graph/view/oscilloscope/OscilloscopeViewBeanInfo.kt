package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoriesType
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class OscilloscopeViewBeanInfo : ComponentBeanInfo<OscilloscopeView>() {

    companion object {
	    private val scale = CommandPropertySwing("timelineScale", "graph.property.oscilloscopeScale", Double::class.java, componentBeanProvider)
	    private val mode = CommandPropertySwing("mode", "graph.property.oscilloscopeMode.type", SignalHistoriesType::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: OscilloscopeView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
	    properties.add(scale.bind(editor, beanIdProvider(bean.id)))
	    properties.add(mode.bind(editor, beanIdProvider(bean.id)))
    }
}