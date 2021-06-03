package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ProbeViewBeanInfo : DigitalComponentBeanInfo<ProbeView>() {

    companion object {
	    private val name = EditProperties.untranslatableName()
	    private val bitWidth = AntaresProperties.bitWidth()
	    private val signalRep = AntaresProperties.signalRepresentation()
	    private val output = CommandPropertySwing("hasOutput", "element.property.hasOutput", Boolean::class.java, componentBeanProvider)
	    private val logging = CommandPropertySwing("logging", "element.property.logging", Boolean::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: ProbeView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(name.bind(editor, bean.id))
	    properties.add(bitWidth.bind(editor, bean.id))
	    properties.add(signalRep.bind(editor, bean.id))
	    properties.add(output.bind(editor, bean.id, editable = !bean.model.isConnected))
	    properties.add(logging.bind(editor, bean.id))
    }
}