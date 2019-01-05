package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.graphics.PredefinedStroke
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

@Suppress("unused")
/** A [BeanInfo] for [QuadCurveComponent]. */
class QuadCurveComponentBeanInfo : AbstractBeanInfo<QuadCurveComponent>() {

	companion object {
		private val filled = PropertyImpl("edit.property.filled", Boolean::class.java)
		private val styleType = PropertyImpl("draw.styleType", StyleType::class.java)
		private val color = PropertyImpl("edit.property.color", PredefinedColor::class.java)
		private val stroke = PropertyImpl("edit.property.stroke", PredefinedStroke::class.java)
		private val shadow = PropertyImpl("edit.property.shadow", Boolean::class.java)
	}

	override fun addProperties(bean: QuadCurveComponent, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		filled.bind(editor, { bean.filled }, { bean.filled = it!! })
		styleType.bind(editor, { bean.styleType }, { bean.styleType = it!! })
		color.bind(editor, { bean.customColor }, { bean.customColor = it })
		stroke.bind(editor, { bean.customStroke}, { bean.customStroke = it } )
		shadow.bind(editor, { bean.shadow }, { bean.customShadow = it!! })

		properties.add(filled)
		properties.add(shadow)
		properties.add(styleType)
		properties.add(color)
		properties.add(stroke)
	}
}