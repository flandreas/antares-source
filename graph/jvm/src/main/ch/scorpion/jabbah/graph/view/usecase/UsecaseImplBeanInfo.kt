package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.graph.view.GraphView
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class UsecaseImplBeanInfo : AbstractBeanInfo<UsecaseImpl>() {

	companion object {
		private val usecaseBeanProvider: BeanProvider = { e, ids -> (e.drawing as GraphView).usecases.get(ids[0]) as Bean }

		private val name = EditProperties.name(baseKey = "graph.property.usecase.name", beanProvider = usecaseBeanProvider)
		private val description = EditProperties.description(beanProvider = usecaseBeanProvider)
	}

	override fun addProperties(bean: UsecaseImpl, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val execScript = EditProperties.script("executionScriptProperty", "graph.property.usecase.execScript",
			beanProvider = usecaseBeanProvider, bean::createParser)
		val testScript = EditProperties.script("testScriptProperty", "graph.property.usecase.testScript",
			beanProvider = usecaseBeanProvider, bean::createParser)

		properties.add(name.bind(editor, bean.id))
		properties.add(description.bind(editor, bean.id))
		properties.add(execScript.bind(editor, bean.id))
		properties.add(testScript.bind(editor, bean.id))
	}
}