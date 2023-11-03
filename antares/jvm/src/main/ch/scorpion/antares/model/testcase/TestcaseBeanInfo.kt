package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.GraphView
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
	}

	override fun addProperties(bean: Testcase, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val testVectors = EditProperties.script("testVectors", "antares.testcase.testVectors",
			beanProvider = testcaseBeanProvider, bean::createParser)

		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(description.bind(editor, beanIdProvider(bean.id)))
		properties.add(testVectors.bind(editor, beanIdProvider(bean.id)))
		properties.add(ignored.bind(editor, beanIdProvider(bean.id)))
	}
}