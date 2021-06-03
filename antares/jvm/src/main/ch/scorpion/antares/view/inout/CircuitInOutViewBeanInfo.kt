package ch.scorpion.antares.view.inout

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class CircuitInOutViewBeanInfo : ComponentBeanInfo<CircuitInOutView>() {

    companion object {
	    private val modelId = GraphProperties.modelId()
	    private val name = EditProperties.untranslatableName()
	    private val portType = AntaresProperties.portType()
	    private val bitWidth = AntaresProperties.bitWidth()
	    private val orientation = EditProperties.orientation()
	    private val color = EditProperties.color()
	    private val signalRepresentation = AntaresProperties.signalRepresentation()
	    private val toggle = CommandPropertySwing("toggle", "element.property.Switch.toggle", Boolean::class.java, componentBeanProvider)
	    private val description = EditProperties.description()
    }

    override fun addProperties(bean: CircuitInOutView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(modelId.bind(editor, bean.id, editable = false))
	    properties.add(name.bind(editor, bean.id))
	    properties.add(portType.bind(editor, bean.id))
	    properties.add(bitWidth.bind(editor, bean.id))
	    properties.add(orientation.bind(editor, bean.id))
	    properties.add(color.bind(editor, bean.id))
	    properties.add(signalRepresentation.bind(editor, bean.id))
	    properties.add(toggle.bind(editor, bean.id))
	    properties.add(description.bind(editor, bean.id))
    }
}