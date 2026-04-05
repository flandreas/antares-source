package io.antarescircuit.jabbah.graph.view.usecase

import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.graph.view.GraphView
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class UsecaseImplBeanInfo : AbstractBeanInfo<UsecaseImpl>() {

	companion object {
		private val usecaseBeanProvider: BeanProvider = { e, ids ->
			listOf((e.drawing as GraphView).usecases.get(ids.iterator().next().toInt()) as Bean)
		}

		private val name = EditProperties.name(baseKey = "graph.property.usecase.name", beanProvider = usecaseBeanProvider)
		private val description = EditProperties.description(beanProvider = usecaseBeanProvider)
	}

	override fun addProperties(bean: UsecaseImpl, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val execScript = EditProperties.script("executionScript", "graph.property.usecase.execScript",
			beanProvider = usecaseBeanProvider, bean::createParser, UsecaseImpl.SCRIPTING_HELP_ID)
		val testScript = EditProperties.script("testScriptProperty", "graph.property.usecase.testScript",
			beanProvider = usecaseBeanProvider, bean::createParser, UsecaseImpl.SCRIPTING_HELP_ID)

		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(description.bind(editor, beanIdProvider(bean.id)))
		properties.add(execScript.bind(editor, beanIdProvider(bean.id)))
		properties.add(testScript.bind(editor, beanIdProvider(bean.id)))
	}
}