package io.antarescircuit.jabbah.base.swing

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.event.PropertyChangeSupport
import io.antarescircuit.jabbah.base.swing.SidebarPaneContent.Companion.DESC_PROPERTY
import io.antarescircuit.jabbah.base.swing.SidebarPaneContent.Companion.ICON_PROPERTY
import io.antarescircuit.jabbah.base.swing.SidebarPaneContent.Companion.NAME_PROPERTY
import javax.swing.Icon
import javax.swing.JComponent

interface SidebarPaneContent {

	companion object {
		const val NAME_PROPERTY = "name"
		const val DESC_PROPERTY = "desc"
		const val ICON_PROPERTY = "icon"
	}

	val name: String

	val description: String? get() = null

	val icon: Icon

	val component: JComponent

	/** The additional [Action]s to be displayed in the title bar of this [SidebarPaneContent].*/
	val actions: List<Action>

	fun addListener (listener: PropertyChangeListener<Any>)

	fun removeListener (listener: PropertyChangeListener<Any>)
}

/**
 * Sent by any object (but particularly [JComponent]) displayed within a [SidebarPaneContent]
 * that it has new interesting content it want to display. [SidebarPane] checks whether any
 * of its [SidebarPaneContent]s contain that [JComponent], and open it if so.
 */
data class ShowSidebarPaneContentRequest(val component: JComponent)

class SidebarPaneContentImpl(
	name: String,
	description: String,
	icon: Icon,
	override val component: JComponent,
	override val actions: List<Action> = listOf()
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

	override var description: String? = description
		set(value) {
			if (value != field) {
				val oldValue = field
				field = value
				support.fire(DESC_PROPERTY, oldValue, value)
			}
		}

	override fun addListener(listener: PropertyChangeListener<Any>) {
		support.add(listener)
	}

	override fun removeListener(listener: PropertyChangeListener<Any>) {
		support.remove(listener)
	}
}