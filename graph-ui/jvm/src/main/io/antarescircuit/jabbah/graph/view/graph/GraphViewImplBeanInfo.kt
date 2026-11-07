package io.antarescircuit.jabbah.graph.view.graph

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.drawingBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.model.semantic.GraphSemantic
import io.antarescircuit.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
open class GraphViewImplBeanInfo<in T: GraphViewImpl> : AbstractBeanInfo<T>() {

    companion object {
	    private val type = CommandPropertySwing("type.typeName", "graph.property.type", String::class.java, drawingBeanProvider)
	    private val name = CommandPropertySwing("translatableName", "graph.property.GraphViewImpl", TranslatableText::class.java, drawingBeanProvider)
	    private val propDelay = GraphProperties.overallPropagationDelay("effectivePropagationDelay", drawingBeanProvider)
	    private val startupTime = GraphProperties.startupTime(drawingBeanProvider)
		private val description = CommandPropertySwing("description", "graph.property.GraphViewImpl.shortDescription", TranslatableText::class.java, drawingBeanProvider)
	    private val purelyScripted = GraphProperties.purelyScripted(drawingBeanProvider)
	    private val paramDefs = GraphProperties.graphParamDefinitions()
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    val script = EditProperties.script("script", "graph.property.GraphViewImpl.script",
		    drawingBeanProvider, bean.graph!!::createParser, GraphViewImpl.SCRIPT_HELP_ID)
	    val ids = listOf<String>()


	    properties.add(type.bind(editor, ids, editable = false))
	    properties.add(name.bind(editor, ids, filter = { false }))

		if (bean.graph?.parameterDefinitions?.hasAnyWithSemantic(GraphSemantic.PropagationDelay) != true) {
			properties.add(propDelay.bind(editor, ids))
		}

	    properties.add(startupTime.bind(editor, ids, optional = true))
	    properties.add(description.bind(editor, ids, filter = { true }))
	    properties.add(script.bind(editor, ids))
	    properties.add(purelyScripted.bind(editor, ids, editable = bean.script.isNotEmpty()))
	    properties.add(paramDefs.bind(editor, ids))
    }
}