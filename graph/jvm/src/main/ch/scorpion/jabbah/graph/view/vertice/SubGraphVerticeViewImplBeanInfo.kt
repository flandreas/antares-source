package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.geom.Direction
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty

@Suppress("unused")
class SubGraphVerticeViewImplBeanInfo : AbstractBeanInfo<SubGraphVerticeViewImpl>() {

    companion object {
        private val id = PropertyImpl("edit.property.id", Int::class.java)
        private val propDelay = PropertyImpl("element.property.propagationDelay", Long::class.java)
	    private val orientation = PropertyImpl("edit.property.Component.orientation", Direction::class.java)
        private val mirrorH = PropertyImpl("graph.property.mirrorHorizontally", Boolean::class.java)
        private val mirrorV = PropertyImpl("graph.property.mirrorVertically", Boolean::class.java)
        private val label = PropertyImpl("graph.property.label", String::class.java)
	    private val description = PropertyImpl("edit.property.description", TextProperty::class.java)
    }

    override fun addProperties(bean: SubGraphVerticeViewImpl, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        id.bind(editor, { bean.id }, null, false)
		propDelay.bind(editor, { bean.propagationDelay }, { bean.propagationDelay = it!! })
	    orientation.bind(editor, {bean.orientation}, {bean.orientation = it!!})
		mirrorH.bind(editor, { bean.isHorizontallyMirrored }, { bean.isHorizontallyMirrored = it!!})
		mirrorV.bind(editor, { bean.isVerticallyMirrored }, { bean.isVerticallyMirrored = it!!})
		label.bind(editor, { bean.label }, { bean.label = it })
	    description.bind(editor, { bean.descriptionProperty }, { bean.descriptionProperty = it!! })

        properties.add(id)
        properties.add(propDelay)
	    properties.add(orientation)
        properties.add(mirrorH)
        properties.add(mirrorV)
        if (bean.label != null) {
            properties.add(label)
        }
	    properties.add(description)
    }
}