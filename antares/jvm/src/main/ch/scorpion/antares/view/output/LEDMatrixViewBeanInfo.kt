package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.signal.BitWidth
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.edit.model.Size

@Suppress("unused")
/**
 * A [BeanInfo] for [LEDMatrixView].
 */
class LEDMatrixViewBeanInfo : ComponentBeanInfo<LEDMatrixView>() {

    companion object {
        private val columnWidth = PropertyImpl("element.property.columns", BitWidth::class.java)
        private val rowWidth = PropertyImpl("element.property.rows", BitWidth::class.java)
        private val lightColor = PropertyImpl("element.property.LEDColor", LightColor::class.java)
        private val size = PropertyImpl("edit.property.size", Size::class.java)
        private val afterglow = PropertyImpl("element.property.LEDMatrix.afterglow", Long::class.java)
        private val isCircleDots = PropertyImpl("element.property.LEDMatrix.isCircleDots", Boolean::class.java)
        private val isDebug = PropertyImpl("element.property.isDebug", Boolean::class.java)
    }

    override fun addProperties(bean: LEDMatrixView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        columnWidth.bind(editor, { bean.columnWidth}, { bean.columnWidth = it!! })
        rowWidth.bind(editor, { bean.rowWidth}, { bean.rowWidth = it!! })
        lightColor.bind(editor, { bean.lightColor }, {bean.lightColor = it!! })
        size.bind(editor, { bean.size }, { bean.size = it!! })
        afterglow.bind(editor, { bean.afterglowDuration}, { bean.afterglowDuration = it!! })
        isCircleDots.bind(editor, { bean.isCircleDots }, { bean.isCircleDots = it!! })
        isDebug.bind(editor, { bean.isDebug }, { bean.isDebug = it!! })

        properties.add(columnWidth)
        properties.add(rowWidth)
        properties.add(lightColor)
        properties.add(size)
        properties.add(afterglow)
        properties.add(isCircleDots)
        properties.add(isDebug)
    }
}