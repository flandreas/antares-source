package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.output.SevenSegmentDisplayScheme
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.container.ControlViewComponentBeanInfo
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SevenSegmentDisplayViewBeanInfo : VerticeViewBeanInfo<SevenSegmentDisplayView>() {

    companion object {
	    private val lightColor = AntaresProperties.lightColor()
	    private val portScheme = CommandPropertySwing("portScheme", "element.property.SevenSegmentDisplayScheme", SevenSegmentDisplayScheme::class.java, componentBeanProvider)
	    private val logic = CommandPropertySwing("logic", "element.property.segmentDisplay.logic", Logic::class.java, componentBeanProvider)
	    private val size = EditProperties.size()
	    private val hasBorder = EditProperties.border()

	    private val controlViewLightColor = AntaresProperties.lightColor(name = "${ControlViewComponentBeanInfo.aggregatePropertyName}.lightColor")
    }

    override fun addProperties(bean: SevenSegmentDisplayView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        val connected = bean.model.isConnected

	    properties.add(lightColor.bind(editor, beanIdProvider(bean.id)))
	    if (bean.size == Size.LARGE) {
		    properties.add(portScheme.bind(editor, beanIdProvider(bean.id), editable = !connected))
	    }
	    properties.add(logic.bind(editor, beanIdProvider(bean.id)))
	    properties.add(size.bind(editor, beanIdProvider(bean.id), editable = !connected))
	    properties.add(hasBorder.bind(editor, beanIdProvider(bean.id)))
    }

	override fun addControlViewProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		super.addControlViewProperties(bean, editor, properties)
		properties.add(controlViewLightColor.bind(editor, beanIdProvider(bean.id)))
	}
}