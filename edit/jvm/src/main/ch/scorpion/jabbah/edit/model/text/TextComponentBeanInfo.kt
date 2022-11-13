package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/**
 * A [BeanInfo] for [TextComponent].
 */
@Suppress("unused")
abstract class TextComponentBeanInfo<T : TextComponent>(
	private val fillAndStroke: Boolean = true
) : AbstractBeanInfo<T>() {

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