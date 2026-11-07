package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.drawingBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImplBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class AnalogGraphViewBeanInfo : GraphViewImplBeanInfo<AnalogGraphView>() {
     companion object {
         private val timeStep = CommandPropertySwing("timeStep", "antares.analog.timeStep", Double::class.java, drawingBeanProvider)
     }

    override fun addProperties(bean: AnalogGraphView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(timeStep.bind(editor, listOf()))
    }
}