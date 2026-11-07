package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ProbeViewBeanInfo : DigitalComponentViewBeanInfo<ProbeView>() {

    companion object {
	    private val name = EditProperties.untranslatableName()
	    private val bitWidth = AntaresProperties.bitWidth()
	    private val signalRep = AntaresProperties.signalRepresentation()
	    private val output = CommandPropertySwing("hasOutput", "element.property.hasOutput", Boolean::class.java, componentBeanProvider)
	    private val logging = CommandPropertySwing("logging", "element.property.logging", Boolean::class.java, componentBeanProvider)
	    private val fixedPointConfigFraction = AntaresProperties.fixedPointConfigFraction()
	    private val fixedPointConfigSigned = AntaresProperties.fixedPointConfigSigned()
    }

    override fun addProperties(bean: ProbeView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(name.bind(editor, beanIdProvider(bean.id)))
	    properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
	    properties.add(signalRep.bind(editor, beanIdProvider(bean.id)))
	    properties.add(output.bind(editor, beanIdProvider(bean.id), editable = !bean.model.isConnected))
	    properties.add(logging.bind(editor, beanIdProvider(bean.id)))
	    if (bean.signalRepresentation == DigitalSignalRepresentation.FIXED_POINT) {
		    properties.add(fixedPointConfigFraction.bind(editor, beanIdProvider(bean.id)))
		    properties.add(fixedPointConfigSigned.bind(editor, beanIdProvider(bean.id)))
	    }
    }
}