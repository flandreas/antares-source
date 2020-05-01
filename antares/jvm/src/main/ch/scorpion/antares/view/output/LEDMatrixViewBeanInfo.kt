package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LEDMatrixViewBeanInfo : ComponentBeanInfo<LEDMatrixView>() {

    companion object {
	    private val columnWidth = PropertyImpl("columnWidth", "element.property.columns", BitWidth::class.java, componentBeanProvider)
	    private val rowWidth = PropertyImpl("rowWidth", "element.property.rows", BitWidth::class.java, componentBeanProvider)
	    private val lightColor = AntaresProperties.lightColor()
	    private val size = EditProperties.size()
	    private val afterglow = PropertyImpl("afterglowDuration", "element.property.LEDMatrix.afterglow", Long::class.java, componentBeanProvider)
	    private val isCircleDots = PropertyImpl("circleDots", "element.property.LEDMatrix.isCircleDots", Boolean::class.java, componentBeanProvider)
	    private val isDebug = PropertyImpl("debug", "element.property.isDebug", Boolean::class.java, componentBeanProvider)
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