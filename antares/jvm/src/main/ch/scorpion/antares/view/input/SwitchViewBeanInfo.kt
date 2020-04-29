package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.graph.view.vertice.VerticeLabelPosition
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** A [BeanInfo] for [SwitchView] */
@Suppress("unused")
class SwitchViewBeanInfo : DigitalComponentBeanInfo<SwitchView>() {

    companion object {
        private val name = PropertyImpl("element.property", String::class.java)
        private val toggle = PropertyImpl("element.property.Switch.toggle", Boolean::class.java)
        private val labelPosition = PropertyImpl("graph.property.VerticeLabelPosition", VerticeLabelPosition::class.java)
    }

    override fun addProperties(bean: SwitchView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        name.bind(editor, { bean.name }, { bean.name = it })
        toggle.bind(editor, { bean.toggle }, { bean.toggle = it!! })
        labelPosition.bind(editor, { bean.labelPosition }, { bean.labelPosition = it!! })

        properties.add(name)
        properties.add(toggle)
        properties.add(labelPosition)
    }
}
