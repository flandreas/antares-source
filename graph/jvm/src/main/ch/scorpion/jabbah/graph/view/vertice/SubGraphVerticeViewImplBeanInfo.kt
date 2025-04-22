package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.AbstractComponentBeanInfo
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.param.GraphParamValuePropertyFactoryRegistry
import ch.scorpion.jabbah.graph.model.semantic.GraphSemantic
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SubGraphVerticeViewImplBeanInfo : AbstractComponentBeanInfo<SubGraphVerticeViewImpl>() {

    companion object {
	    private val modelId = GraphProperties.modelId()
	    private val propDelay = GraphProperties.propagationDelay(editable = false)
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
			properties.add(propDelay.bind(editor, beanIdProvider(bean.id)))
		}

	    properties.add(orientation.bind(editor, beanIdProvider(bean.id)))
	    properties.add(mirrorH.bind(editor, beanIdProvider(bean.id)))
	    properties.add(mirrorV.bind(editor, beanIdProvider(bean.id)))
	    properties.add(color.bind(editor, beanIdProvider(bean.id)))
	    properties.add(controlViewVisibility.bind(editor, beanIdProvider(bean.id)))
	    bean.label?.let {
	        properties.add(label.bind(editor, beanIdProvider(bean.id), filter = { false }))
	    }
	    properties.add(description.bind(editor, beanIdProvider(bean.id)))

	    paramDefs?.let { defs ->
			for (def in defs.iterator()) {
				val property = GraphParamValuePropertyFactoryRegistry.createProperty(def, editor, componentBeanProvider)
				properties.add(property.bind(editor, beanIdProvider(bean.id)))
			}
		}
    }
}