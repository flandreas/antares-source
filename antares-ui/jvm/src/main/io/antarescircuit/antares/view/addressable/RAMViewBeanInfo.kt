package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class RAMViewBeanInfo : DigitalComponentViewBeanInfo<RAMView>() {

    companion object {
	    private val addressBitWidth = AntaresProperties.bitWidth("addressWidth", "element.property.addressBitWidth")
	    private val dataBitWidth = AntaresProperties.bitWidth("dataWidth", "element.property.dataBitWidth")
	    private val text = GraphProperties.label(name = "text")
	    private val clock = CommandPropertySwing("hasClock", "element.property.RAM.clock", Boolean::class.java, componentBeanProvider)
		private val nonVolatile = GraphProperties.nonVolatile()
	    private val showContents = CommandPropertySwing("showContents", "element.property.Addressable.showContents", Boolean::class.java, componentBeanProvider)
	    private val contentsRowCount = CommandPropertySwing("contentRowsCount", "element.property.Addressable.rowsCount", Int::class.java, componentBeanProvider)
	    private val contentsColumnsCount = CommandPropertySwing("contentColumnsCount", "element.property.Addressable.columnsCount", Int::class.java, componentBeanProvider)
		private val separateDataPorts = CommandPropertySwing("separateDataPorts", "element.property.RAM.separateDataPorts", Boolean::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: RAMView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(addressBitWidth.bind(editor, beanIdProvider(bean.id)))
	    properties.add(dataBitWidth.bind(editor, beanIdProvider(bean.id)))
	    properties.add(separateDataPorts.bind(editor, beanIdProvider(bean.id), editable = !bean.model.isConnected))
	    properties.add(clock.bind(editor, beanIdProvider(bean.id), editable = !bean.model.isConnected))
	    properties.add(text.bind(editor, beanIdProvider(bean.id), filter = { false }))
		properties.add(nonVolatile.bind(editor, beanIdProvider(bean.id)))
	    properties.add(showContents.bind(editor, beanIdProvider(bean.id)))
	    if (bean.showContents) {
		    properties.add(contentsRowCount.bind(editor, beanIdProvider(bean.id)))
		    properties.add(contentsColumnsCount.bind(editor, beanIdProvider(bean.id)))
	    }
    }
}