package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** A [BeanInfo] for [ScenarioImpl].*/
@Suppress("unused")
class ScenarioImplBeanInfo : AbstractBeanInfo<ScenarioImpl>() {

	private val name = PropertyImpl("graph.property.scenario.name", TranslatableText::class.java)
	private val description = PropertyImpl("edit.property.description", TranslatableText::class.java)
	private val condition = PropertyImpl("graph.property.scenario.condition", ScriptProperty::class.java)

	override fun addProperties(bean: ScenarioImpl, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		name.bind(editor, { bean.name.translation }, { bean.name.translation = it!! }, true, { false })
		description.bind(editor, { bean.description.translation }, { bean.description.translation = it!! }, true, { true })
		condition.bind(editor, { bean.conditionProperty }, { bean.conditionProperty = it!! })

		properties.add(name)
		properties.add(description)
		properties.add(condition)
	}
}