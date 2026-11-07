package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.container.ControlViewComponentBeanInfo
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LEDMatrixViewBeanInfo : VerticeViewBeanInfo<LEDMatrixView>() {

    companion object {
	    private val columnWidth = AntaresProperties.bitWidth("columnWidth", "element.property.columns")
	    private val rowWidth = AntaresProperties.bitWidth("rowWidth", "element.property.rows")
	    private val lightColor = AntaresProperties.lightColor()
	    private val size = EditProperties.size()
	    private val afterglow = CommandPropertySwing("afterglowDuration", "element.property.LEDMatrix.afterglow", Long::class.java, componentBeanProvider)
	    private val isCircleDots = CommandPropertySwing("circleDots", "element.property.LEDMatrix.isCircleDots", Boolean::class.java, componentBeanProvider)
	    private val isDebug = CommandPropertySwing("debug", "element.property.isDebug", Boolean::class.java, componentBeanProvider)

	    private val controlViewLightColor = AntaresProperties.lightColor(name = "${ControlViewComponentBeanInfo.aggregatePropertyName}.lightColor")
    }

    override fun addProperties(bean: LEDMatrixView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(columnWidth.bind(editor, beanIdProvider(bean.id)))
	    properties.add(rowWidth.bind(editor, beanIdProvider(bean.id)))
	    properties.add(lightColor.bind(editor, beanIdProvider(bean.id)))
	    properties.add(size.bind(editor, beanIdProvider(bean.id)))
	    properties.add(afterglow.bind(editor, beanIdProvider(bean.id)))
	    properties.add(isCircleDots.bind(editor, beanIdProvider(bean.id)))
	    properties.add(isDebug.bind(editor, beanIdProvider(bean.id)))
    }

	override fun addControlViewProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		super.addControlViewProperties(bean, editor, properties)
		properties.add(controlViewLightColor.bind(editor, beanIdProvider(bean.id)))
	}
}