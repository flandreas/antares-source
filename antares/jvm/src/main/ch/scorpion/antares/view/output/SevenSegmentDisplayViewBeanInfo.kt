package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SevenSegmentDisplayViewBeanInfo : VerticeViewBeanInfo<SevenSegmentDisplayView>() {

    companion object {
	    private val name = EditProperties.untranslatableName()
	    private val lightColor = AntaresProperties.lightColor()
	    private val portScheme = PropertyImpl("portScheme", "element.property.SevenSegmentDisplayScheme", SevenSegmentDisplayScheme::class.java, componentBeanProvider)
	    private val size = EditProperties.size()
	    private val hasBorder = PropertyImpl("hasBorder", "element.property.SevenSegmentDisplayView.hasBorder", Boolean::class.java, componentBeanProvider)
    }

	init {
		isShowColor = false
	}

    override fun addProperties(bean: SevenSegmentDisplayView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        val connected = bean.model.isConnected

	    properties.add(name.bind(editor, bean.id))
	    properties.add(lightColor.bind(editor, bean.id))
	    properties.add(portScheme.bind(editor, bean.id, editable = !connected))
	    properties.add(size.bind(editor, bean.id, editable = !connected))
	    properties.add(hasBorder.bind(editor, bean.id))
    }
}