package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SwitchViewBeanInfo : DigitalComponentBeanInfo<SwitchView>() {

    companion object {
	    private val name = AntaresProperties.untranslatableName()
	    private val toggle = PropertyImpl("toggle", "element.property.Switch.toggle", Boolean::class.java, componentBeanProvider)
	    private val labelPosition = GraphProperties.verticalLabelPosition()

    }

    override fun addProperties(bean: SwitchView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(name.bind(editor, bean.id))
	    properties.add(toggle.bind(editor, bean.id))
	    properties.add(labelPosition.bind(editor, bean.id))
    }
}
