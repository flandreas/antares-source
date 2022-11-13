package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.graph.view.GraphView
import com.l2fprod.common.propertysheet.Property


@Suppress("unused")
class ScenarioImplBeanInfo : AbstractBeanInfo<ScenarioImpl>() {

	companion object {

		private val scenarioBeanProvider: BeanProvider = { e, ids ->
			listOf((e.drawing as GraphView).scenarios.get(ids.iterator().next().toInt()) as Bean)
		}

		private val name = EditProperties.name(baseKey = "graph.property.scenario.name", beanProvider = scenarioBeanProvider)
		private val description = EditProperties.description(baseKey = "graph.property.scenario.description", beanProvider = scenarioBeanProvider)
	}

	override fun addProperties(bean: ScenarioImpl, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val condition = EditProperties.script("conditionProperty", "graph.property.scenario.condition",
			beanProvider = scenarioBeanProvider, bean::createParser)

		properties.add(name.bind(editor, beanIdProvider(bean.id), filter = { false }))
		properties.add(description.bind(editor, beanIdProvider(bean.id)))
		properties.add(condition.bind(editor, beanIdProvider(bean.id)))
	}
}