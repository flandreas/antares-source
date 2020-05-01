package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TunnelViewBeanInfo : DigitalComponentBeanInfo<TunnelView>() {

    companion object {
	    private val name = AntaresProperties.untranslatableName()
	    private val bitWidth = AntaresProperties.bitWidth()
    }

    override fun addProperties(bean: TunnelView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(name.bind(editor, bean.id))
	    properties.add(bitWidth.bind(editor, bean.id))
    }
}