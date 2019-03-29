package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class UsecaseImplBeanInfo : AbstractBeanInfo<UsecaseImpl>() {

	private val name = PropertyImpl("graph.property.usecase.name", TranslatableText::class.java)
	private val description = PropertyImpl("edit.property.description", TranslatableText::class.java)
	private val execScript = PropertyImpl("graph.property.usecase.execScript", TextProperty::class.java)
	private val testScript = PropertyImpl("graph.property.usecase.testScript", TextProperty::class.java)

	override fun addProperties(bean: UsecaseImpl, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		name.bind(editor, { bean.name.translation }, { bean.name.translation = it!! }, true, { false })
		description.bind(editor, { bean.description.translation }, { bean.description.translation = it!! }, true, { true })
		execScript.bind(editor, { bean.executionScriptProperty }, { bean.executionScriptProperty = it!! })
		testScript.bind(editor, { bean.testScriptProperty }, { bean.testScriptProperty = it!! })

		properties.add(name)
		properties.add(description)
		properties.add(execScript)
		properties.add(testScript)
	}
}