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
    }

    override fun addProperties(bean: TunnelView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(name.bind(editor, bean.id))
	    properties.add(AntaresProperties.bitWidth(editor = editor).bind(editor, bean.id))
    }
}