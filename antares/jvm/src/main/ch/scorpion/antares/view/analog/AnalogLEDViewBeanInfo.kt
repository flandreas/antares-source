package ch.scorpion.antares.view.analog

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class AnalogLEDViewBeanInfo : AnalogComponentViewBeanInfo<AnalogLEDView>() {

    companion object {
        private val lightColor = AntaresProperties.lightColor(baseKey = "element.property.LightColor")
    }

    override fun addProperties(bean: AnalogLEDView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(lightColor.bind(editor, beanIdProvider(bean.id)))
    }

    override var isShowColor: Boolean
        get() = false
        set(value) {
            super.isShowColor = value
        }
}