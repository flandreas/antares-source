package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamValuePropertyFactoryRegistry
import io.antarescircuit.jabbah.graph.model.semantic.GraphSemantic
import io.antarescircuit.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SubGraphVerticeViewImplBeanInfo : AbstractComponentBeanInfo<SubGraphVerticeViewImpl>() {

    companion object {
	    private val modelId = GraphProperties.modelId()
	    private val propDelay = GraphProperties.propagationDelay()
	    private val color = EditProperties.color()
	    private val orientation = EditProperties.orientation()
	    private val mirrorH = CommandPropertySwing("horizontallyMirrored", "graph.property.mirrorHorizontally", Boolean::class.java, componentBeanProvider)
	    private val mirrorV = CommandPropertySwing("verticallyMirrored", "graph.property.mirrorVertically", Boolean::class.java, componentBeanProvider)
	    private val label = GraphProperties.label()
	    private val description = EditProperties.description()
	    private val controlViewVisibility = GraphProperties.controlViewVisibility()
    }

    override fun addProperties(bean: SubGraphVerticeViewImpl, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(modelId.bind(editor, beanIdProvider(bean.id), editable = false))

	    if (bean.model.designError != null) {
		    return
	    }

		val paramDefs = bean.model.graphUUID?.let {
			LibraryModule.libraryHolder.getMetaGraph(it).graph.model?.parameterDefinitions
		}

		if (paramDefs == null || !paramDefs.hasAnyWithSemantic(GraphSemantic.PropagationDelay)) {
			properties.add(propDelay.bind(editor, beanIdProvider(bean.id), editable = false))
		}

	    properties.add(orientation.bind(editor, beanIdProvider(bean.id)))
	    properties.add(mirrorH.bind(editor, beanIdProvider(bean.id)))
	    properties.add(mirrorV.bind(editor, beanIdProvider(bean.id)))
	    properties.add(color.bind(editor, beanIdProvider(bean.id)))
	    properties.add(controlViewVisibility.bind(editor, beanIdProvider(bean.id)))
	    bean.label?.let {
	        properties.add(label.bind(editor, beanIdProvider(bean.id), filter = { false }))
	    }

	    paramDefs?.let { defs ->
			for (def in defs.iterator()) {
				val property = GraphParamValuePropertyFactoryRegistry.createProperty(def, editor, componentBeanProvider)
				properties.add(property.bind(editor, beanIdProvider(bean.id)))
			}
		}

		properties.add(description.bind(editor, beanIdProvider(bean.id)))
    }
}