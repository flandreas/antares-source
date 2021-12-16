package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class RAMViewBeanInfo : DigitalComponentBeanInfo<RAMView>() {

    companion object {
	    private val addressBitWidth = AntaresProperties.bitWidth("addressWidth", "element.property.addressBitWidth")
	    private val dataBitWidth = AntaresProperties.bitWidth("dataWidth", "element.property.dataBitWidth")
	    private val text = GraphProperties.label(name = "text")
	    private val clock = CommandPropertySwing("hasClock", "element.property.RAM.clock", Boolean::class.java, componentBeanProvider)
	    private val showContents = CommandPropertySwing("showContents", "element.property.Addressable.showContents", Boolean::class.java, componentBeanProvider)
	    private val contentsRowCount = CommandPropertySwing("contentRowsCount", "element.property.Addressable.rowsCount", Int::class.java, componentBeanProvider)
	    private val contentsColumnsCount = CommandPropertySwing("contentColumnsCount", "element.property.Addressable.columnsCount", Int::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: RAMView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(addressBitWidth.bind(editor, bean.id))
	    properties.add(dataBitWidth.bind(editor, bean.id))
	    properties.add(text.bind(editor, bean.id, filter = { false }))
	    properties.add(clock.bind(editor, bean.id))
	    properties.add(showContents.bind(editor, bean.id))
	    if (bean.showContents) {
		    properties.add(contentsRowCount.bind(editor, bean.id))
		    properties.add(contentsColumnsCount.bind(editor, bean.id))
	    }
    }
}