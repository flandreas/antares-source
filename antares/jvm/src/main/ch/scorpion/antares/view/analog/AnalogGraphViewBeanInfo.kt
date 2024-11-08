package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.drawingBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.graph.GraphViewImplBeanInfo
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