package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.GraphView
import com.l2fprod.common.propertysheet.Property


@Suppress("unused")
class ScenarioStepImplBeanInfo : AbstractBeanInfo<ScenarioStepImpl>() {

	companion object {
		private val scenarioStepProvider: BeanProvider = { e, ids -> (e.drawing as GraphView).scenarios.get(ids[0]).getStep(ids[1]) as Bean }

		private val name = EditProperties.name(baseKey = "graph.property.scenario.name", beanProvider = scenarioStepProvider)
		private val description = EditProperties.description(beanProvider = scenarioStepProvider)
		private val highlightIds = CommandPropertySwing("highlightIds", "graph.property.scenario.highlightIds", String::class.java, scenarioStepProvider)
	}

	override fun addProperties(bean: ScenarioStepImpl, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val scenarios = (editor.drawing as GraphView).scenarios
		val scenario = scenarios.getScenarios().first { it.getScenarioSteps().contains(bean) }
		val ids = listOf(scenario.id, bean.id)

		val condition = EditProperties.script("conditionProperty", "graph.property.scenario.condition",
			beanProvider = scenarioStepProvider, bean::createParser)
		val onEntry = EditProperties.script("onEntryProperty", "graph.property.scenario.onEntry",
			beanProvider = scenarioStepProvider, bean::createParser)
		val onExit = EditProperties.script("onExitProperty", "graph.property.scenario.onExit",
			beanProvider = scenarioStepProvider, bean::createParser)

		properties.add(name.bind(editor, ids, filter = { false }))
		properties.add(description.bind(editor, ids))
		properties.add(condition.bind(editor, ids))
		properties.add(highlightIds.bind(editor, ids))
		properties.add(onEntry.bind(editor, ids))
		properties.add(onExit.bind(editor, ids))
	}
}