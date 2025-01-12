package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo


/** A [BeanInfo] for [PolylineComponent].*/
@Suppress("unused")
class PolylineComponentBeanInfo : AbstractBeanInfo<PolylineComponent>() {

    companion object {
	    private val filled = EditProperties.filled()
	    private val styleType = EditProperties.styleType()
	    private val color = EditProperties.color()
	    private val stroke = EditProperties.stroke()
	    private val shadow = EditProperties.shadow()
		private val arrow = CommandPropertySwing("arrow", PolylineComponent.BASE_KEY_ARROW, Boolean::class.java, componentBeanProvider)
		private val mirrorH = EditProperties.horizontallyMirrored()
		private val mirrorV = EditProperties.verticallyMirrored()
    }

    override fun addProperties(bean: PolylineComponent, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(filled.bind(editor, beanIdProvider(bean.id)))
	    properties.add(styleType.bind(editor, beanIdProvider(bean.id)))
	    properties.add(color.bind(editor, beanIdProvider(bean.id)))
	    properties.add(stroke.bind(editor, beanIdProvider(bean.id)))
	    properties.add(shadow.bind(editor, beanIdProvider(bean.id)))
	    properties.add(arrow.bind(editor, beanIdProvider(bean.id)))
		properties.add(mirrorH.bind(editor, beanIdProvider(bean.id)))
		properties.add(mirrorV.bind(editor, beanIdProvider(bean.id)))
    }
}