package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.GraphView
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TestcaseBeanInfo : AbstractBeanInfo<Testcase>() {

	companion object {

		private val testcaseBeanProvider: BeanProvider = { e, ids ->
			listOf(((e.drawing as GraphView).graph as DigitalGraph).testcases.get(ids.iterator().next().toInt()) as Bean)
		}

		private val name = EditProperties.name(beanProvider = testcaseBeanProvider)
		private val description = EditProperties.description(beanProvider = testcaseBeanProvider)
		private val ignored = CommandPropertySwing("ignored", "antares.testcase.ignored", Boolean::class.java, testcaseBeanProvider)
		private val skipPropDelayConsistenceCheck = CommandPropertySwing("skipPropDelayConsistenceCheck", "antares.testcase.skipPropDelayConsistencyTest", Boolean::class.java, testcaseBeanProvider)
		private val numberOfIterations = CommandPropertySwing("numberOfIterations", "antares.testcase.numberOfIterations", Int::class.java, beanProvider = testcaseBeanProvider)
	}

	override fun addProperties(bean: Testcase, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val testVectors = EditProperties.script("testVectors", "antares.testcase.testVectors",
			beanProvider = testcaseBeanProvider, bean::createParser, Testcase.SCRIPT_HELP_ID)

		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(description.bind(editor, beanIdProvider(bean.id)))
		properties.add(testVectors.bind(editor, beanIdProvider(bean.id)))
		properties.add(ignored.bind(editor, beanIdProvider(bean.id)))
		properties.add(skipPropDelayConsistenceCheck.bind(editor, beanIdProvider(bean.id)))
		properties.add(numberOfIterations.bind(editor, beanIdProvider(bean.id)))
	}
}