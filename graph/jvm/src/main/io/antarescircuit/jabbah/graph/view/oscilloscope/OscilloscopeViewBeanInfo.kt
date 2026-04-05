package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class OscilloscopeViewBeanInfo : AbstractComponentBeanInfo<OscilloscopeView>() {

    companion object {
	    private val scale = CommandPropertySwing("persistentTimelineScale", "graph.property.oscilloscopeScale", Double::class.java, componentBeanProvider)
	    private val mode = CommandPropertySwing("mode", "graph.property.oscilloscopeMode.type", SignalHistoriesType::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: OscilloscopeView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
	    properties.add(scale.bind(editor, beanIdProvider(bean.id)))
	    properties.add(mode.bind(editor, beanIdProvider(bean.id)))
    }
}