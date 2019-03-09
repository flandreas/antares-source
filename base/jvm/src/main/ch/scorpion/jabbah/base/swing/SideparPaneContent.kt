package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyChangeSupport
import ch.scorpion.jabbah.base.swing.SidebarPaneContent.Companion.ICON_PROPERTY
import ch.scorpion.jabbah.base.swing.SidebarPaneContent.Companion.NAME_PROPERTY
import javax.swing.Icon
import javax.swing.JComponent

interface SidebarPaneContent {

	companion object {
		const val NAME_PROPERTY = "name"
		const val ICON_PROPERTY = "icon"
	}

	val name: String

	val icon: Icon

	val component: JComponent

	fun addListener (listener: PropertyChangeListener<Any>)

	fun removeListener (listener: PropertyChangeListener<Any>)

}

class SidebarPaneContentImpl(
	name: String,
	icon: Icon,
	override val component: JComponent
) : SidebarPaneContent {

	private val support = PropertyChangeSupport<Any>(this)

	override var name: String = name
		set(value) {
			if (value != field) {
				val oldValue = field
				field = value
				support.fire(NAME_PROPERTY, oldValue, value)
			}
		}

	override var icon: Icon = icon
		set(value) {
			if (value != field) {
				val oldValue = field
				field = value
				support.fire(ICON_PROPERTY, oldValue, value)
			}
		}

	override fun addListener(listener: PropertyChangeListener<Any>) {
		support.add(listener)
	}

	override fun removeListener(listener: PropertyChangeListener<Any>) {
		support.remove(listener)
	}
}