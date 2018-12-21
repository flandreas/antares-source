package ch.scorpion.jabbah.graph.view.graph

import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText


/**
 * A [BeanInfo] for [GraphViewImpl].
 */
class GraphViewImplBeanInfo : AbstractBeanInfo<GraphViewImpl<*>>() {

    companion object {
        private val name = PropertyImpl("graph.property.GraphViewImpl", TranslatableText::class.java)
        private val propDelay = PropertyImpl("element.property.propagationDelay", Long::class.java)
        private val shortDesc = PropertyImpl("graph.property.GraphViewImpl.shortDescription", TranslatableText::class.java)
        private val script = PropertyImpl("graph.property.GraphViewImpl.script", TextProperty::class.java)
    }

    override fun addProperties(bean: GraphViewImpl<*>, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        name.bind(editor, { bean.translatableName }, { bean.translatableName = it!! }, true, { false })
		propDelay.bind(editor, { bean.propagationDelay }, { bean.propagationDelay = it })
		shortDesc.bind(editor, { bean.translatableShortDescription }, { bean.translatableShortDescription = it!! }, true, { true })
		script.bind(editor, { bean.script }, { bean.script = it!! })

		properties.add(name)
		properties.add(propDelay)
		properties.add(shortDesc)
		properties.add(script)
    }
}