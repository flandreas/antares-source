package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
abstract class TextComponentBeanInfo<T : TextComponent>(
	private val fillAndStroke: Boolean = true
) : AbstractComponentBeanInfo<T>() {

	companion object {
		private val filled = EditProperties.filled()
		private val stroked = EditProperties.stroked()
		private val styleType = EditProperties.styleType()
		private val color = EditProperties.color()
	}

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		if (fillAndStroke) {
			properties.add(filled.bind(editor, beanIdProvider(bean.id)))
			properties.add(stroked.bind(editor, beanIdProvider(bean.id)))
		}
		properties.add(styleType.bind(editor, beanIdProvider(bean.id)))
		properties.add(color.bind(editor, beanIdProvider(bean.id)))
	}
}

@Suppress("unused")
class LabelComponentBeanInfo : TextComponentBeanInfo<LabelComponent>(fillAndStroke = false) {

	companion object {
		private val text = EditProperties.translatableText()
	}

	override fun addProperties(bean: LabelComponent, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(text.bind(editor, beanIdProvider(bean.id), filter = { false }))
	}
}

@Suppress("unused")
class TextComponentJvmBeanInfo : TextComponentBeanInfo<TextComponentJvm>() {

	companion object {
		private val text = EditProperties.translatableText()
		private val horizontalAlignment = EditProperties.horizontalAlignment()
	}

	override fun addProperties(bean: TextComponentJvm, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(text.bind(editor, beanIdProvider(bean.id), filter = { true }))
		properties.add(horizontalAlignment.bind(editor, beanIdProvider(bean.id)))
	}
}