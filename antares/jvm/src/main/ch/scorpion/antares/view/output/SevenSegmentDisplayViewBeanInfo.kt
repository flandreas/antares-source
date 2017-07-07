package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.Size


/**
 * A [BeanInfo] for [SevenSegmentDisplayView].
 */
class SevenSegmentDisplayViewBeanInfo : AbstractBeanInfo<SevenSegmentDisplayView>() {

    companion object {
        private val name = PropertyImpl("element.property", String::class.java)
        private val lightColor = PropertyImpl("element.property.LEDColor", LightColor::class.java)
        private val portScheme = PropertyImpl("element.property.SevenSegmentDisplayScheme", SevenSegmentDisplayScheme::class.java)
        private val size = PropertyImpl("edit.property.size", Size::class.java)
    }

    override fun addProperties(bean: SevenSegmentDisplayView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        val connected = bean.model!!.isConnected

		name.bind(editor, { bean.name }, { bean.name = it })
		lightColor.bind(editor, { bean.lightColor }, {bean.lightColor = it!! })
		portScheme.bind(editor, { bean.portScheme }, { bean.portScheme = it!! }, !connected)
		size.bind(editor, { bean.size }, { bean.size = it!! }, !connected)

		properties.add(name)
		properties.add(lightColor)
		if (bean.size == Size.LARGE) {
			properties.add(portScheme)
		}
		properties.add(size)
    }
}