package ch.scorpion.jabbah.graph.view.graph

import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText


/**
 * A [BeanInfo] for [GraphViewImpl].
 */
@Suppress("unused")
open class GraphViewImplBeanInfo<in T: GraphViewImpl> : AbstractBeanInfo<T>() {

    companion object {
        private val name = PropertyImpl("graph.property.GraphViewImpl", TranslatableText::class.java)
        private val propDelay = PropertyImpl("element.property.propagationDelay", Long::class.java)
        private val description = PropertyImpl("graph.property.GraphViewImpl.shortDescription", TranslatableText::class.java)
        private val script = PropertyImpl("graph.property.GraphViewImpl.script", ScriptProperty::class.java)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        name.bind(editor, { bean.translatableName }, { bean.translatableName = it!! }, true, { false })
		propDelay.bind(editor, { bean.propagationDelay }, { bean.propagationDelay = it })
		description.bind(editor, { bean.description }, { bean.description = it!! }, true, { true })
		script.bind(editor, { bean.script }, { bean.script = it!! })

		properties.add(name)
		properties.add(propDelay)
		properties.add(description)
		properties.add(script)
    }
}