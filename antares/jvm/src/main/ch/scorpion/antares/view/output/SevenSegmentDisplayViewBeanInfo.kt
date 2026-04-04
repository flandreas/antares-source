package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.ControlViewComponentBeanInfo
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
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