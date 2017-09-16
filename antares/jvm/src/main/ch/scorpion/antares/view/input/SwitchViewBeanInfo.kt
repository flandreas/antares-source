package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import com.l2fprod.common.propertysheet.Property

/** A [BeanInfo] for [SwitchView] */
class SwitchViewBeanInfo : DigitalComponentBeanInfo<SwitchView>() {

    companion object {
        private val name = PropertyImpl("element.property", String::class.java)
    }

    override fun addProperties(bean: SwitchView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        name.bind(editor, { bean.name }, { bean.name = it })
        properties.add(name)
    }
}

/** A [BeanInfo] for [ToggleButtonView] */
class ToggleButtonViewBeanInfo : DigitalComponentBeanInfo<ToggleButtonView>()