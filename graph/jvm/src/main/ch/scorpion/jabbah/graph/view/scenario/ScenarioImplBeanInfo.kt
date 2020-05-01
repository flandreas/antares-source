package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.graph.view.GraphView
import com.l2fprod.common.propertysheet.Property


@Suppress("unused")
class ScenarioImplBeanInfo : AbstractBeanInfo<ScenarioImpl>() {

	companion object {
		private val scenarioBeanProvider: BeanProvider = { e, ids -> (e.drawing as GraphView).scenarios.get(ids[0]) as Bean }

		private val name = EditProperties.name(baseKey = "graph.property.scenario.name", beanProvider = scenarioBeanProvider)
		private val description = EditProperties.description(beanProvider = scenarioBeanProvider)
		private val condition = PropertyImpl("conditionProperty", "graph.property.scenario.condition", ScriptProperty::class.java, scenarioBeanProvider)
	}

	override fun addProperties(bean: ScenarioImpl, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(name.bind(editor, bean.id, filter = { false }))
		properties.add(description.bind(editor, bean.id))
		properties.add(condition.bind(editor, bean.id))
	}
}