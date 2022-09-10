package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SevenSegmentDisplayViewBeanInfo : VerticeViewBeanInfo<SevenSegmentDisplayView>() {

    companion object {
	    private val name = EditProperties.untranslatableName()
	    private val lightColor = AntaresProperties.lightColor()
	    private val portScheme = CommandPropertySwing("portScheme", "element.property.SevenSegmentDisplayScheme", SevenSegmentDisplayScheme::class.java, componentBeanProvider)
	    private val logic = CommandPropertySwing("logic", "element.property.segmentDisplay.logic", Logic::class.java, componentBeanProvider)
	    private val size = EditProperties.size()
	    private val hasBorder = EditProperties.border()
    }

	init {
		isShowColor = false
	}

    override fun addProperties(bean: SevenSegmentDisplayView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        val connected = bean.model.isConnected

	    properties.add(name.bind(editor, bean.id))
	    properties.add(lightColor.bind(editor, bean.id))
	    if (bean.size == Size.LARGE) {
		    properties.add(portScheme.bind(editor, bean.id, editable = !connected))
	    }
	    properties.add(logic.bind(editor, bean.id))
	    properties.add(size.bind(editor, bean.id, editable = !connected))
	    properties.add(hasBorder.bind(editor, bean.id))
    }
}