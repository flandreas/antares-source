package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class DigitalCircuitInOutViewBeanInfo : AbstractCircuitInOutViewBeanInfo<DigitalCircuitInOutView>() {

    companion object {
	    private val bitWidth = AntaresProperties.bitWidth()
	    private val signalRepresentation = AntaresProperties.signalRepresentation()
	    private val toggle = CommandPropertySwing("toggle", SwitchView.BASE_KEY_TOGGLE, Boolean::class.java, componentBeanProvider)
	    private val canBeUndefined = AntaresProperties.canBeUndefined()
	    private val description = EditProperties.description()
		private val startValue = GraphProperties.graphPortStartValue()
		private val interactivePropagationDelay = GraphProperties.interactivePropagationDelay()
    }

    override fun addProperties(bean: DigitalCircuitInOutView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
	    properties.add(signalRepresentation.bind(editor, beanIdProvider(bean.id), filter = { it != DigitalSignalRepresentation.FIXED_POINT }))
	    if (bean.model.portType.isInput) {
		    properties.add(toggle.bind(editor, beanIdProvider(bean.id)))
			properties.add(startValue.bind(editor, beanIdProvider(bean.id), optional = true))
			properties.add(interactivePropagationDelay.bind(editor, beanIdProvider(bean.id)))
	    }
	    if (bean.model.portType == PortType.OUTPUT) {
			properties.add(canBeUndefined.bind(editor, beanIdProvider(bean.id)))
	    }
	    properties.add(description.bind(editor, beanIdProvider(bean.id)))
    }
}