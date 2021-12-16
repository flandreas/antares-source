package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
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
    }

	init {
		isShowColor = false
	}

    override fun addProperties(bean: LEDMatrixView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(columnWidth.bind(editor, bean.id))
	    properties.add(rowWidth.bind(editor, bean.id))
	    properties.add(lightColor.bind(editor, bean.id))
	    properties.add(size.bind(editor, bean.id))
	    properties.add(afterglow.bind(editor, bean.id))
	    properties.add(isCircleDots.bind(editor, bean.id))
	    properties.add(isDebug.bind(editor, bean.id))
    }
}