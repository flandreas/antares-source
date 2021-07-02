package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.drawingBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
open class GraphViewImplBeanInfo<in T: GraphViewImpl> : AbstractBeanInfo<T>() {

    companion object {
	    private val name = CommandPropertySwing("translatableName", "graph.property.GraphViewImpl", TranslatableText::class.java, drawingBeanProvider)
	    private val propDelay = GraphProperties.propagationDelay(drawingBeanProvider)
		private val description = CommandPropertySwing("description", "graph.property.GraphViewImpl.shortDescription", TranslatableText::class.java, drawingBeanProvider)
	    private val script = EditProperties.script("script", "graph.property.GraphViewImpl.script", beanProvider = drawingBeanProvider)
	    private val purelyScripted = GraphProperties.purelyScripted(drawingBeanProvider)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    val ids = listOf<Int>()

	    properties.add(name.bind(editor, ids, filter = { false }))
	    properties.add(propDelay.bind(editor, ids))
	    properties.add(description.bind(editor, ids, filter = { true }))
	    properties.add(script.bind(editor, ids))
	    properties.add(purelyScripted.bind(editor, ids, editable = bean.script.isNotEmpty()))
    }
}