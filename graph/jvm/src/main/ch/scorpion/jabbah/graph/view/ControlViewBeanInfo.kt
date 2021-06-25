package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import com.l2fprod.common.propertysheet.Property

/**
 * Implemented by [AbstractBeanInfo] subclasses for beans that implement [ControlView]
 * in order to allow the user to also edit properties of the [ControlView], not only
 * those of [ControlViewComponent].
 */
interface ControlViewBeanInfo {
	fun addControlViewProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>)
}