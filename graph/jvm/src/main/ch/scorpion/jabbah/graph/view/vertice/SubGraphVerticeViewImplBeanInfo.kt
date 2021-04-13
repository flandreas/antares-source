package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SubGraphVerticeViewImplBeanInfo : ComponentBeanInfo<SubGraphVerticeViewImpl>() {

    companion object {
	    private val modelId = GraphProperties.modelId()
	    private val propDelay = GraphProperties.propagationDelay()
	    private val orientation = EditProperties.orientation()
	    private val mirrorH = PropertyImpl("horizontallyMirrored", "graph.property.mirrorHorizontally", Boolean::class.java, componentBeanProvider)
	    private val mirrorV = PropertyImpl("verticallyMirrored", "graph.property.mirrorVertically", Boolean::class.java, componentBeanProvider)
	    private val label = GraphProperties.label()
	    private val description = EditProperties.description()
    }

	private val isShowModelId: Boolean get() = EditAuthModule.userHolder.user.isDeveloper

    override fun addProperties(bean: SubGraphVerticeViewImpl, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    if (isShowModelId) {
		    properties.add(modelId.bind(editor, bean.id, editable = false))
	    }
	    properties.add(propDelay.bind(editor, bean.id))
	    properties.add(orientation.bind(editor, bean.id))
	    properties.add(mirrorH.bind(editor, bean.id))
	    properties.add(mirrorV.bind(editor, bean.id))
	    bean.label?.let {
	        properties.add(label.bind(editor, bean.id, filter = { false }))
	    }
	    properties.add(description.bind(editor, bean.id))
    }
}