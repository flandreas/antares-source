package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import java.beans.BeanInfo

/**
 * A [BeanInfo] for [LEDView].
 */
@Suppress("unused")
class LEDViewBeanInfo : DigitalComponentBeanInfo<LEDView>() {

    companion object {
        private val name = PropertyImpl("element.property", String::class.java)
        val lightColor = PropertyImpl("element.property.LEDColor", LightColor::class.java)
    }

    override fun addProperties(bean: LEDView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        name.bind(editor, { bean.name }, { bean.name = it })
        lightColor.bind(editor, {bean.lightColor}, {bean.lightColor = it!!})

        properties.add(name)
        properties.add(lightColor)
    }

	override var isShowColor: Boolean
		get() = false
		set(value) { super.isShowColor = value}
}