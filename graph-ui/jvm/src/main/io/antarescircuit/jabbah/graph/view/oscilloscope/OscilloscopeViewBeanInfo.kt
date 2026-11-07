package io.antarescircuit.jabbah.graph.view.oscilloscope

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import io.antarescircuit.jabbah.graph.view.GraphView

@Suppress("unused")
class OscilloscopeViewBeanInfo : AbstractComponentBeanInfo<OscilloscopeView>() {

    companion object {
	    private val scale = CommandPropertySwing("persistentTimelineScale", "graph.property.oscilloscopeScale", Double::class.java, componentBeanProvider)
	    private val mode = CommandPropertySwing("mode", "graph.property.oscilloscopeMode.type", SignalHistoriesType::class.java, componentBeanProvider)
        private val bufferSize = CommandPropertySwing("bufferSize", "graph.property.oscilloscope.bufferSize", Int::class.java, componentBeanProvider)
        private val signalCurveStyle = CommandPropertySwing("signalCurveStyle", "graph.property.oscilloscope.signalCurveStyle",
            OscilloscopeSignalCurveStyle::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: OscilloscopeView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
	    properties.add(scale.bind(editor, beanIdProvider(bean.id)))
	    properties.add(mode.bind(editor, beanIdProvider(bean.id)))
        properties.add(bufferSize.bind(editor, beanIdProvider(bean.id)))
        if ((editor.drawing as GraphView).graph?.type?.supportOscilloscopeSignalCurveStyleSelection == true) {
            properties.add(signalCurveStyle.bind(editor, beanIdProvider(bean.id)))
        }
    }
}