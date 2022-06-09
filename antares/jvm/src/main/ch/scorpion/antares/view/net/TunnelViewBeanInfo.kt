package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TunnelViewBeanInfo : DigitalComponentBeanInfo<TunnelView>() {

    companion object {
	    private val name = EditProperties.untranslatableName()
	    private val bitWidth = AntaresProperties.bitWidth()
	    private val flowDirection = AntaresProperties.tunnelFlowDirection()
    }

    override fun addProperties(bean: TunnelView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(name.bind(editor, bean.id))
	    properties.add(bitWidth.bind(editor, bean.id))
	    if (TunnelView.face == TunnelViewFace.ARROW) {
		    properties.add(flowDirection.bind(editor, bean.id))
	    }
    }
}